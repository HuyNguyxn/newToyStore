package com.example.new_toy_store.customer_return.application.service;

import com.example.new_toy_store.customer_return.application.dto.request.CustomerReturnRequest;
import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnResponse;
import com.example.new_toy_store.customer_return.domain.CustomerReturn;
import com.example.new_toy_store.customer_return.domain.CustomerReturnItem;
import com.example.new_toy_store.customer_return.domain.CustomerReturnRepository;
import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import com.example.new_toy_store.customer_return.domain.ReturnReasonCode;
import com.example.new_toy_store.customer_return.domain.exception.CustomerReturnNotFoundException;
import com.example.new_toy_store.customer_return.domain.exception.DuplicateReturnRequestException;
import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnDataException;
import com.example.new_toy_store.customer_return.mapper.CustomerReturnMapper;
import com.example.new_toy_store.global.event.CustomerReturnStatusChangedEvent;
import com.example.new_toy_store.infrastructure.specification.CustomerReturnSpecification;
import com.example.new_toy_store.order.application.facade.OrderFacade;
import com.example.new_toy_store.product.application.facade.ProductFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final OrderFacade orderFacade;
    private final ProductFacade productFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.customer-return.auto-reject.expiration-days:7}")
    private int expirationDays;

    public CustomerReturnService(CustomerReturnRepository repository,
                                 OrderFacade orderFacade,
                                 ProductFacade productFacade,
                                 ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.orderFacade = orderFacade;
        this.productFacade = productFacade;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<CustomerReturnResponse> filterReturns(String status, Integer orderId, Pageable pageable) {
        return repository.findAll(CustomerReturnSpecification.filter(status, orderId), pageable)
                .map(CustomerReturnMapper::toResponse);
    }

    @Transactional
    public CustomerReturnResponse createRequest(CustomerReturnRequest request, Integer customerId, String customerUsername) {
        orderFacade.verifyOrderOwnership(request.getOrderId(), customerId);

        String currentOrderStatus = orderFacade.getOrderStatus(request.getOrderId());
        if (!"COMPLETED".equals(currentOrderStatus)) {
            throw InvalidCustomerReturnDataException.invalidOrderStatus(currentOrderStatus);
        }

        if (repository.hasActiveReturnRequest(request.getOrderId())) {
            throw new DuplicateReturnRequestException(request.getOrderId());
        }

        boolean hasDefectiveItem = request.getItems().stream()
                .anyMatch(item -> ReturnReasonCode.from(item.getReasonCode()) == ReturnReasonCode.DEFECTIVE
                        || ReturnReasonCode.from(item.getReasonCode()) == ReturnReasonCode.WRONG_ITEM);

        if (hasDefectiveItem && (request.getProofImageUrls() == null || request.getProofImageUrls().isEmpty())) {
            throw InvalidCustomerReturnDataException.missingProofImage();
        }

        CustomerReturn rma = CustomerReturnMapper.toEntity(request, customerUsername);

        if (orderFacade.isHighRiskCustomer(customerId)) {
            rma.markAsHighRisk("CẢNH BÁO: Tài khoản này có tỷ lệ hoàn hàng >= 30%. Vui lòng kiểm định minh chứng hình ảnh nghiêm ngặt.");
        }

        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse cancelRequest(Integer id, Integer customerId, String customerUsername) {
        CustomerReturn rma = getEntity(id);
        orderFacade.verifyOrderOwnership(rma.getOrderId(), customerId);
        CustomerReturnStatus previousStatus = rma.getStatus();
        rma.cancelByUser(customerUsername, "Khách hàng tự hủy yêu cầu");
        CustomerReturn saved = repository.save(rma);
        publishStatusChanged(saved, previousStatus, customerUsername);
        return CustomerReturnMapper.toResponse(saved);
    }

    @Transactional
    public CustomerReturnResponse updateInfoByCustomer(Integer id, Integer customerId, String customerUsername, String newReasonNote) {
        CustomerReturn rma = getEntity(id);
        orderFacade.verifyOrderOwnership(rma.getOrderId(), customerId);
        CustomerReturnStatus previousStatus = rma.getStatus();
        rma.updateInfoFromCustomer(customerUsername, newReasonNote);
        CustomerReturn saved = repository.save(rma);
        publishStatusChanged(saved, previousStatus, customerUsername);
        return CustomerReturnMapper.toResponse(saved);
    }

    @Transactional
    public CustomerReturnResponse requireMoreInfo(Integer id, String adminUsername, String adminMessage) {
        CustomerReturn rma = getEntity(id);
        CustomerReturnStatus previousStatus = rma.getStatus();
        rma.requireMoreInfo(adminUsername, adminMessage, this.expirationDays);
        CustomerReturn saved = repository.save(rma);
        publishStatusChanged(saved, previousStatus, adminUsername);
        return CustomerReturnMapper.toResponse(saved);
    }

    @Transactional
    public CustomerReturnResponse receiveItems(Integer id, String warehouseUsername) {
        CustomerReturn rma = getEntity(id);
        CustomerReturnStatus previousStatus = rma.getStatus();
        rma.receiveItems(warehouseUsername, "Kho đã xác nhận nhận hàng, chờ kiểm định QC.");
        CustomerReturn saved = repository.save(rma);
        publishStatusChanged(saved, previousStatus, warehouseUsername);
        return CustomerReturnMapper.toResponse(saved);
    }

    @Transactional
    public CustomerReturnResponse inspectQuality(Integer id, String qcUsername, boolean isPassed, String qcNote) {
        CustomerReturn rma = getEntity(id);
        CustomerReturnStatus previousStatus = rma.getStatus();

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
                productFacade.restoreStockForCancelledOrder(sellableItems);
            }
        } else {
            rma.failQualityControl(qcUsername, qcNote);
        }

        CustomerReturn saved = repository.save(rma);
        publishStatusChanged(saved, previousStatus, qcUsername);
        return CustomerReturnMapper.toResponse(saved);
    }

    @Transactional
    public CustomerReturnResponse createDispute(Integer id, Integer customerId, String customerUsername, String disputeReason) {
        CustomerReturn rma = getEntity(id);
        orderFacade.verifyOrderOwnership(rma.getOrderId(), customerId);
        CustomerReturnStatus previousStatus = rma.getStatus();
        rma.openDispute(customerUsername, disputeReason);
        CustomerReturn saved = repository.save(rma);
        publishStatusChanged(saved, previousStatus, customerUsername);
        return CustomerReturnMapper.toResponse(saved);
    }

    @Transactional
    public CustomerReturnResponse resolveDispute(Integer id, String adminUsername, boolean isApproved, String resolutionNote) {
        CustomerReturn rma = getEntity(id);
        CustomerReturnStatus previousStatus = rma.getStatus();
        rma.resolveDispute(adminUsername, isApproved, resolutionNote);
        CustomerReturn saved = repository.save(rma);
        publishStatusChanged(saved, previousStatus, adminUsername);
        return CustomerReturnMapper.toResponse(saved);
    }

    @Transactional
    public CustomerReturnResponse finalizeRefundProcess(Integer id, String adminUsername, String note) {
        CustomerReturn rma = getEntity(id);
        CustomerReturnStatus previousStatus = rma.getStatus();
        rma.finalizeRefund(adminUsername, note);
        syncRefundStatusWithOrderDomain(rma);
        CustomerReturn saved = repository.save(rma);
        publishStatusChanged(saved, previousStatus, adminUsername);
        return CustomerReturnMapper.toResponse(saved);
    }

    @Scheduled(cron = "${app.customer-return.auto-reject.cron:0 0 0 * * ?}", zone = "${app.customer-return.auto-reject.zone:Asia/Ho_Chi_Minh}")
    @Transactional
    public void autoRejectExpiredRequests() {
        List<CustomerReturn> expiredList = repository.findExpiredRequests(LocalDateTime.now());
        for (CustomerReturn req : expiredList) {
            CustomerReturnStatus previousStatus = req.getStatus();
            req.rejectReturn("SYSTEM", "Tự động từ chối do quá hạn " + expirationDays + " ngày không bổ sung thông tin.");
            publishStatusChanged(req, previousStatus, "SYSTEM");
        }
        repository.saveAll(expiredList);
    }

    private void syncRefundStatusWithOrderDomain(CustomerReturn rma) {
        Map<Integer, Integer> returnedItemsQty = rma.getItems().stream()
                .collect(Collectors.toMap(
                        CustomerReturnItem::getOrderItemId,
                        CustomerReturnItem::getQuantity
                ));
        orderFacade.updateOrderRefundStatus(rma.getOrderId(), returnedItemsQty);
    }

    private void publishStatusChanged(CustomerReturn rma, CustomerReturnStatus previousStatus, String actionBy) {
        if (previousStatus != rma.getStatus()) {
            eventPublisher.publishEvent(CustomerReturnStatusChangedEvent.now(
                    rma.getId(),
                    rma.getOrderId(),
                    previousStatus,
                    rma.getStatus(),
                    actionBy
            ));
        }
    }

    private CustomerReturn getEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new CustomerReturnNotFoundException(id));
    }
}
