package com.example.new_toy_store.customer_return.application.service;

import com.example.new_toy_store.customer_return.application.dto.request.CustomerReturnRequest;
import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnResponse;
import com.example.new_toy_store.customer_return.domain.*;
import com.example.new_toy_store.customer_return.domain.exception.*;
import com.example.new_toy_store.infrastructure.specification.CustomerReturnSpecification;
import com.example.new_toy_store.customer_return.mapper.CustomerReturnMapper;
import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.product.application.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomerReturnService {

    private final CustomerReturnRepository repository;
    private final OrderService orderService;
    private final ProductService productService;

    @Value("${app.customer-return.auto-reject.expiration-days:7}")
    private int expirationDays;

    public CustomerReturnService(CustomerReturnRepository repository, OrderService orderService, ProductService productService) {
        this.repository = repository;
        this.orderService = orderService;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public Page<CustomerReturnResponse> filterReturns(String status, Integer orderId, Pageable pageable) {
        return repository.findAll(CustomerReturnSpecification.filter(status, orderId), pageable)
                .map(CustomerReturnMapper::toResponse);
    }

    @Transactional
    public CustomerReturnResponse createRequest(CustomerReturnRequest request, Integer customerId, String customerUsername) {
        orderService.verifyOrderOwnership(request.getOrderId(), customerId);

        String currentOrderStatus = orderService.getOrderStatus(request.getOrderId());
        if (!"COMPLETED".equals(currentOrderStatus)) {
            throw InvalidCustomerReturnDataException.invalidOrderStatus(currentOrderStatus);
        }

        if (repository.hasActiveReturnRequest(request.getOrderId())) {
            throw new DuplicateReturnRequestException(request.getOrderId());
        }

        boolean hasDefectiveItem = request.getItems().stream()
                .anyMatch(i -> ReturnReasonCode.from(i.getReasonCode()) == ReturnReasonCode.DEFECTIVE
                        || ReturnReasonCode.from(i.getReasonCode()) == ReturnReasonCode.WRONG_ITEM);

        if (hasDefectiveItem && (request.getProofImageUrls() == null || request.getProofImageUrls().isEmpty())) {
            throw InvalidCustomerReturnDataException.missingProofImage();
        }

        CustomerReturn rma = CustomerReturnMapper.toEntity(request, customerUsername);

        if (orderService.isHighRiskCustomer(customerId)) {
            rma.markAsHighRisk("CẢNH BÁO: Tài khoản này có tỷ lệ hoàn hàng >= 30%. Vui lòng kiểm định minh chứng hình ảnh nghiêm ngặt.");
        }

        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse cancelRequest(Integer id, Integer customerId, String customerUsername) {
        CustomerReturn rma = getEntity(id);
        orderService.verifyOrderOwnership(rma.getOrderId(), customerId);
        rma.cancelByUser(customerUsername, "Khách hàng tự hủy yêu cầu");
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse updateInfoByCustomer(Integer id, Integer customerId, String customerUsername, String newReasonNote) {
        CustomerReturn rma = getEntity(id);
        orderService.verifyOrderOwnership(rma.getOrderId(), customerId);
        rma.updateInfoFromCustomer(customerUsername, newReasonNote);
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse requireMoreInfo(Integer id, String adminUsername, String adminMessage) {
        CustomerReturn rma = getEntity(id);
        rma.requireMoreInfo(adminUsername, adminMessage, this.expirationDays);
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse receiveItems(Integer id, String warehouseUsername) {
        CustomerReturn rma = getEntity(id);
        rma.receiveItems(warehouseUsername, "Kho đã xác nhận nhận hàng, chờ kiểm định QC.");
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse inspectQuality(Integer id, String qcUsername, boolean isPassed, String qcNote) {
        CustomerReturn rma = getEntity(id);

        if (isPassed) {
            rma.passQualityControl(qcUsername, qcNote);
            Map<Integer, Integer> sellableItems = rma.getItems().stream()
                    .filter(item -> item.getReasonCode().isSellable())
                    .collect(Collectors.toMap(
                            CustomerReturnItem::getVariantId,
                            CustomerReturnItem::getQuantity,
                            Integer::sum
                    ));

            if (!sellableItems.isEmpty()) {
                productService.restoreStockForCancelledOrder(sellableItems);
            }
        } else {
            rma.failQualityControl(qcUsername, qcNote);
        }

        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse createDispute(Integer id, Integer customerId, String customerUsername, String disputeReason) {
        CustomerReturn rma = getEntity(id);
        orderService.verifyOrderOwnership(rma.getOrderId(), customerId);
        rma.openDispute(customerUsername, disputeReason);
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse resolveDispute(Integer id, String adminUsername, boolean isApproved, String resolutionNote) {
        CustomerReturn rma = getEntity(id);
        rma.resolveDispute(adminUsername, isApproved, resolutionNote);
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse finalizeRefundProcess(Integer id, String adminUsername, String note) {
        CustomerReturn rma = getEntity(id);
        rma.finalizeRefund(adminUsername, note);
        syncRefundStatusWithOrderDomain(rma);
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Scheduled(cron = "${app.customer-return.auto-reject.cron:0 0 0 * * ?}", zone = "${app.customer-return.auto-reject.zone:Asia/Ho_Chi_Minh}")
    @Transactional
    public void autoRejectExpiredRequests() {
        List<CustomerReturn> expiredList = repository.findExpiredRequests(LocalDateTime.now());
        for (CustomerReturn req : expiredList) {
            req.rejectReturn("SYSTEM", "Tự động từ chối do quá hạn " + expirationDays + " ngày không bổ sung thông tin.");
        }
        repository.saveAll(expiredList);
    }

    private void syncRefundStatusWithOrderDomain(CustomerReturn rma) {
        Map<Integer, Integer> returnedItemsQty = rma.getItems().stream()
                .collect(Collectors.toMap(
                        CustomerReturnItem::getOrderItemId,
                        CustomerReturnItem::getQuantity
                ));
        orderService.updateOrderRefundStatus(rma.getOrderId(), returnedItemsQty);
    }

    private CustomerReturn getEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new CustomerReturnNotFoundException(id));
    }
}