package com.example.new_toy_store.customer_return.application;

import com.example.new_toy_store.customer_return.application.dto.request.CustomerReturnRequest;
import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnResponse;
import com.example.new_toy_store.customer_return.domain.*;
import com.example.new_toy_store.customer_return.domain.exception.*;
import com.example.new_toy_store.infrastructure.specification.CustomerReturnSpecification;
import com.example.new_toy_store.customer_return.mapper.CustomerReturnMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerReturnService {

    private final CustomerReturnRepository repository;

    @Value("${app.customer-return.auto-reject.expiration-days:7}")
    private int expirationDays;

    public CustomerReturnService(CustomerReturnRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<CustomerReturnResponse> filterReturns(String status, Integer orderId, Pageable pageable) {
        return repository.findAll(CustomerReturnSpecification.filter(status, orderId), pageable)
                .map(CustomerReturnMapper::toResponse);
    }

    @Transactional
    public CustomerReturnResponse createRequest(CustomerReturnRequest request, String customerUsername) {
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
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse cancelRequest(Integer id, String customerUsername) {
        CustomerReturn rma = getEntity(id);
        validateActionAccess(rma, customerUsername, "Hủy yêu cầu trả hàng");

        rma.cancelByUser(customerUsername, "Khách hàng tự hủy yêu cầu");
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse updateInfoByCustomer(Integer id, String customerUsername, String newReasonNote) {
        CustomerReturn rma = getEntity(id);
        validateActionAccess(rma, customerUsername, "Cập nhật thông tin");

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
        } else {
            rma.failQualityControl(qcUsername, qcNote);
        }
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse createDispute(Integer id, String customerUsername, String disputeReason) {
        CustomerReturn rma = getEntity(id);
        validateActionAccess(rma, customerUsername, "Mở khiếu nại (Dispute)");

        rma.openDispute(customerUsername, disputeReason);
        return CustomerReturnMapper.toResponse(repository.save(rma));
    }

    @Transactional
    public CustomerReturnResponse resolveDisputeToRefund(Integer id, String adminUsername, String resolutionNote) {
        CustomerReturn rma = getEntity(id);
        rma.finalizeRefund(adminUsername, resolutionNote);
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

    private CustomerReturn getEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new CustomerReturnNotFoundException(id));
    }

    private void validateActionAccess(CustomerReturn rma, String currentUser, String actionName) {
        boolean isOwner = rma.getHistories().stream()
                .findFirst()
                .map(h -> h.getActionBy().equals(currentUser))
                .orElse(false);
        if (!isOwner) {
            throw new CustomerReturnAccessDeniedException(currentUser, actionName);
        }
    }
}