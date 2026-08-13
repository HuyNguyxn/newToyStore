package com.example.new_toy_store.customer_payment.application.service;

import com.example.new_toy_store.global.event.PaymentCancelledEvent;
import com.example.new_toy_store.global.event.PaymentCompletedEvent;
import com.example.new_toy_store.global.event.PaymentFailedEvent;
import com.example.new_toy_store.global.event.PaymentRefundedEvent;
import com.example.new_toy_store.global.event.ShipmentDeliveredEvent;
import com.example.new_toy_store.global.event.OrderStatusChangedEvent;
import com.example.new_toy_store.infrastructure.specification.PaymentSpecification;
import com.example.new_toy_store.order.application.dto.response.OrderPaymentSnapshot;
import com.example.new_toy_store.order.application.facade.OrderFacade;
import com.example.new_toy_store.order.domain.OrderStatus;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentCancelRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentCheckoutRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentConfirmRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentFailureRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentFilterRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentRefundDecisionRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentRefundRequest;
import com.example.new_toy_store.customer_payment.application.dto.response.CustomerPaymentRefundResponse;
import com.example.new_toy_store.customer_payment.application.dto.response.CustomerPaymentResponse;
import com.example.new_toy_store.customer_payment.application.dto.response.VnpayReturnResponse;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentMethod;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentRefund;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentRefundRepository;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentRepository;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentStatus;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentTransaction;
import com.example.new_toy_store.customer_payment.domain.RefundMethod;
import com.example.new_toy_store.customer_payment.domain.RefundStatus;
import com.example.new_toy_store.customer_payment.domain.exception.DuplicateActiveCustomerCustomerPaymentException;
import com.example.new_toy_store.customer_payment.domain.exception.DuplicateCustomerCustomerPaymentRefundException;
import com.example.new_toy_store.customer_payment.domain.exception.InvalidCustomerCustomerPaymentOperationException;
import com.example.new_toy_store.customer_payment.domain.exception.CustomerCustomerPaymentAccessDeniedException;
import com.example.new_toy_store.customer_payment.domain.exception.CustomerCustomerPaymentCrossModuleException;
import com.example.new_toy_store.customer_payment.domain.exception.CustomerCustomerPaymentDeletedConflictException;
import com.example.new_toy_store.customer_payment.domain.exception.CustomerPaymentNotFoundException;
import com.example.new_toy_store.customer_payment.domain.exception.CustomerPaymentRefundDeletedConflictException;
import com.example.new_toy_store.customer_payment.domain.exception.CustomerPaymentRefundNotFoundException;
import com.example.new_toy_store.infrastructure.payment.vnpay.VnpayIpnResponse;
import com.example.new_toy_store.infrastructure.payment.vnpay.VnpayRefundResponse;
import com.example.new_toy_store.infrastructure.payment.vnpay.VnpayService;
import com.example.new_toy_store.customer_payment.mapper.CustomerPaymentMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final List<CustomerPaymentStatus> ACTIVE_PAYMENT_STATUSES = List.of(CustomerPaymentStatus.PENDING, CustomerPaymentStatus.SUCCEEDED);

    private final CustomerPaymentRepository repository;
    private final CustomerPaymentRefundRepository refundRepository;
    private final OrderFacade orderFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final VnpayService vnpayService;

    public PaymentService(
            CustomerPaymentRepository repository,
            CustomerPaymentRefundRepository refundRepository,
            OrderFacade orderFacade,
            ApplicationEventPublisher eventPublisher,
            VnpayService vnpayService
    ) {
        this.repository = repository;
        this.refundRepository = refundRepository;
        this.orderFacade = orderFacade;
        this.eventPublisher = eventPublisher;
        this.vnpayService = vnpayService;
    }

    @Transactional
    public CustomerPaymentResponse checkout(CustomerPaymentCheckoutRequest request, Integer currentUserId, boolean isAdmin, String clientIp) {
        CustomerPaymentResponse existingPayment = findExistingIdempotentPayment(request, currentUserId);
        if (existingPayment != null) {
            return existingPayment;
        }

        OrderPaymentSnapshot order = orderFacade.getPaymentSnapshot(request.getOrderId());
        validateOrderCanBePaid(order);
        validateOwnership(order.getOrderId(), order.getUserId(), currentUserId, isAdmin, "checkout");
        
        CustomerPaymentTransaction activePayment = repository.findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(order.getOrderId(), ACTIVE_PAYMENT_STATUSES).orElse(null);
        if (activePayment != null) {
            if (activePayment.getMethod() == CustomerPaymentMethod.VNPAY && activePayment.getStatus() == CustomerPaymentStatus.PENDING) {
                String paymentUrl = vnpayService.createPaymentUrl(activePayment, clientIp);
                return CustomerPaymentMapper.toCheckoutResponse(activePayment, paymentUrl, "Open this paymentUrl to pay with VNPay sandbox.");
            }
            throw new DuplicateActiveCustomerCustomerPaymentException(order.getOrderId());
        }

        CustomerPaymentTransaction payment = new CustomerPaymentTransaction(
                order.getOrderId(),
                order.getUserId(),
                request.getMethod(),
                order.getPayableAmount()
        );
        payment.attachIdempotencyKey(request.getIdempotencyKey());

        repository.save(payment);
        if (payment.getMethod() == CustomerPaymentMethod.VNPAY) {
            String paymentUrl = vnpayService.createPaymentUrl(payment, clientIp);
            return CustomerPaymentMapper.toCheckoutResponse(payment, paymentUrl, "Open this paymentUrl to pay with VNPay sandbox.");
        }
        return CustomerPaymentMapper.toResponse(payment);
    }

    private CustomerPaymentResponse findExistingIdempotentPayment(CustomerPaymentCheckoutRequest request, Integer currentUserId) {
        String idempotencyKey = sanitizeIdempotencyKey(request.getIdempotencyKey());
        if (idempotencyKey == null) {
            return null;
        }

        return repository.findByUserIdAndIdempotencyKey(currentUserId, idempotencyKey)
                .filter(payment -> payment.getOrderId().equals(request.getOrderId()))
                .filter(payment -> payment.getMethod() == request.getMethod())
                .map(CustomerPaymentMapper::toResponse)
                .orElse(null);
    }

    private String sanitizeIdempotencyKey(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    @Transactional(readOnly = true)
    public CustomerPaymentResponse getDetails(Integer paymentId, Integer currentUserId, boolean isAdmin) {
        CustomerPaymentTransaction payment = getPayment(paymentId);
        validateOwnership(payment.getId(), payment.getUserId(), currentUserId, isAdmin, "view");
        return CustomerPaymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<CustomerPaymentResponse> getMyPayments(Integer userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(CustomerPaymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerPaymentResponse> filter(CustomerPaymentFilterRequest request, Pageable pageable) {
        Specification<CustomerPaymentTransaction> spec = PaymentSpecification.filter(request);
        return repository.findAll(spec, pageable).map(CustomerPaymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public boolean hasSucceededPaymentForOrder(Integer orderId) {
        return repository.existsByOrderIdAndStatusIn(orderId, List.of(
                CustomerPaymentStatus.SUCCEEDED,
                CustomerPaymentStatus.PARTIALLY_REFUNDED,
                CustomerPaymentStatus.REFUNDED
        ));
    }

    @Transactional
    public CustomerPaymentRefundResponse requestRefund(Integer paymentId, CustomerPaymentRefundRequest request) {
        CustomerPaymentTransaction payment = getPaymentForUpdate(paymentId);
        validateRefundablePayment(payment);
        validateRefundMethod(payment, request.getMethod());
        validateRefundAmount(payment, request.getAmount());

        CustomerPaymentRefund refund = new CustomerPaymentRefund(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                generateRefundCode(payment.getId()),
                request.getMethod(),
                request.getAmount(),
                request.getReason()
        );

        payment.requestRefund();
        refundRepository.save(refund);
        repository.save(payment);
        return CustomerPaymentMapper.toRefundResponse(refund);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CustomerPaymentRefundResponse requestRefundForCustomerReturn(Integer returnId, Integer orderId, double amount, String reason) {
        String existingRefundCode = "REF-RETURN-" + returnId;
        CustomerPaymentRefund existingRefund = refundRepository.findFirstByRefundCodeStartingWithOrderByCreatedAtDesc(existingRefundCode).orElse(null);
        if (existingRefund != null) {
            return CustomerPaymentMapper.toRefundResponse(existingRefund);
        }

        CustomerPaymentTransaction payment = repository.findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(
                        orderId,
                      List.of(CustomerPaymentStatus.SUCCEEDED, CustomerPaymentStatus.PARTIALLY_REFUNDED)
                )
                .orElseThrow(() -> CustomerCustomerPaymentCrossModuleException.invalidOrder(orderId, "order has no succeeded payment to refund"));

        RefundMethod refundMethod = payment.getMethod() == CustomerPaymentMethod.VNPAY
                ? RefundMethod.VNPAY
                : RefundMethod.COD_MANUAL;

        validateRefundablePayment(payment);
        validateRefundAmount(payment, amount);

        String refundCode = generateCustomerReturnRefundCode(payment.getId(), returnId);

        CustomerPaymentRefund refund = new CustomerPaymentRefund(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                refundCode,
                refundMethod,
                amount,
                reason == null || reason.isBlank() ? "Hoàn tiền từ phiếu trả hàng #" + returnId : reason
        );

        payment.requestRefund();
        refundRepository.save(refund);
        repository.save(payment);
        return CustomerPaymentMapper.toRefundResponse(refund);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requestRefundForCancelledOrder(Integer orderId, String reason) {
        CustomerPaymentTransaction payment = repository.findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(
                orderId,
                List.of(CustomerPaymentStatus.SUCCEEDED, CustomerPaymentStatus.PARTIALLY_REFUNDED)
        ).orElse(null);
        if (payment == null) {
            return;
        }

        String refundCodePrefix = "REF-CANCELLED-ORDER-" + orderId;
        if (refundRepository.findFirstByRefundCodeStartingWithOrderByCreatedAtDesc(refundCodePrefix).isPresent()) {
            return;
        }

        double alreadyRefunded = refundRepository.sumAmountByPaymentIdAndStatuses(
                payment.getId(), List.of(RefundStatus.SUCCEEDED));
        double refundableAmount = Math.max(0.0,
                Math.round((payment.getAmount() - alreadyRefunded) * 100.0) / 100.0);
        if (refundableAmount <= 0) {
            return;
        }

        RefundMethod refundMethod = payment.getMethod() == CustomerPaymentMethod.VNPAY
                ? RefundMethod.VNPAY
                : RefundMethod.COD_MANUAL;
        CustomerPaymentRefund refund = new CustomerPaymentRefund(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                refundCodePrefix + "-PAY-" + payment.getId(),
                refundMethod,
                refundableAmount,
                reason == null || reason.isBlank()
                        ? "Hoàn tiền do đơn hàng #" + orderId + " đã hủy"
                        : reason
        );
        payment.requestRefund();
        refundRepository.save(refund);
        repository.save(payment);
    }

    @Transactional
    public CustomerPaymentRefundResponse processRefund(Integer refundId, String adminEmail, String clientIp) {
        CustomerPaymentRefund refund = getRefundForUpdate(refundId);
        CustomerPaymentTransaction payment = getPaymentForUpdate(refund.getPaymentId());

        if (payment.getStatus() == CustomerPaymentStatus.REFUND_FAILED) {
            payment.requestRefund();
        }
        refund.startProcessing();
        if (refund.getMethod() == RefundMethod.VNPAY) {
            VnpayRefundResponse gatewayResponse = vnpayService.requestRefund(payment, refund, adminEmail, clientIp);
            if (gatewayResponse.isSuccess()) {
                refund.succeed(gatewayResponse.getProviderRefundId());
                completeSuccessfulRefund(payment, refund);
                publishPaymentRefunded(refund);
            } else {
                refund.fail(gatewayResponse.getMessage());
                payment.failRefund();
            }
        } else {
            refund.succeed(refund.getRefundCode());
            completeSuccessfulRefund(payment, refund);
            publishPaymentRefunded(refund);
        }

        refundRepository.save(refund);
        repository.save(payment);
        return CustomerPaymentMapper.toRefundResponse(refund);
    }

    @Transactional
    public CustomerPaymentRefundResponse rejectRefund(Integer refundId, CustomerPaymentRefundDecisionRequest request) {
        CustomerPaymentRefund refund = getRefundForUpdate(refundId);
        CustomerPaymentTransaction payment = getPaymentForUpdate(refund.getPaymentId());
        refund.reject(request.getReason());
        refundRepository.saveAndFlush(refund);
        payment.cancelRefund(hasPreviousSuccessfulRefund(payment.getId()));
        repository.save(payment);
        return CustomerPaymentMapper.toRefundResponse(refund);
    }

    @Transactional(readOnly = true)
    public Page<CustomerPaymentRefundResponse> getRefunds(Integer paymentId, Pageable pageable) {
        return refundRepository.findByPaymentId(paymentId, pageable).map(CustomerPaymentMapper::toRefundResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerPaymentRefundResponse> getAllRefunds(Pageable pageable) {
        return refundRepository.findAll(pageable).map(CustomerPaymentMapper::toRefundResponse);
    }

    @Transactional
    public void deleteRefund(Integer refundId) {
        CustomerPaymentRefund refund = getRefund(refundId);
        int updatedRows = refundRepository.softDeleteWithVersion(
                refundId,
                refund.getVersion(),
                List.of(RefundStatus.REJECTED, RefundStatus.CANCELLED, RefundStatus.FAILED)
        );
        if (updatedRows == 0) {
            throw new CustomerPaymentRefundDeletedConflictException(refundId);
        }
    }

    @Transactional
    public CustomerPaymentResponse markSucceeded(Integer paymentId, CustomerPaymentConfirmRequest request) {
        CustomerPaymentTransaction payment = getPaymentForUpdate(paymentId);
        if (payment.getMethod() == CustomerPaymentMethod.COD) {
            throw new InvalidCustomerCustomerPaymentOperationException(
                    "markSucceeded",
                    "COD payment can only be marked succeeded after shipment delivery."
            );
        }
        payment.succeed(request == null ? null : request.getProviderTransactionId());
        repository.save(payment);

        eventPublisher.publishEvent(PaymentCompletedEvent.now(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMethod(),
                payment.getAmount(),
                orderFacade.getPaymentSnapshot(payment.getOrderId()).getCostAmount(),
                payment.getProviderTransactionId()
        ));
        return CustomerPaymentMapper.toResponse(payment);
    }

    @Transactional
    public void recordCodCollected(ShipmentDeliveredEvent event) {
        if (event.codAmount() <= 0) {
            return;
        }

        recordCodCollected(event.orderId(), "COD-" + event.trackingCode());
    }

    @Transactional
    public void recordCodCollected(OrderStatusChangedEvent event) {
        if (event.currentStatus() != OrderStatus.COMPLETED) {
            return;
        }

        recordCodCollected(event.orderId(), "COD-ORDER-" + event.orderId());
    }

    private void recordCodCollected(Integer orderId, String collectionReference) {
        CustomerPaymentTransaction payment = repository.findByOrderIdAndMethod(orderId, CustomerPaymentMethod.COD)
                .orElse(null);

        if (payment == null || payment.getStatus() == CustomerPaymentStatus.SUCCEEDED) {
            return;
        }
        if (payment.getStatus() != CustomerPaymentStatus.PENDING) {
            throw new InvalidCustomerCustomerPaymentOperationException(
                    "collectCod",
                    "COD payment must be pending before collection can be recorded."
            );
        }

        payment.collectCod(collectionReference);
        repository.save(payment);
        publishPaymentCompleted(payment);
    }

    @Transactional
    public int reconcileCompletedCodPayments() {
        List<CustomerPaymentTransaction> payments = repository.findPendingCodPaymentsForCompletedOrders();
        payments.forEach(payment -> {
            payment.collectCod("COD-ORDER-" + payment.getOrderId());
            repository.save(payment);
            publishPaymentCompleted(payment);
        });
        return payments.size();
    }

    @Transactional
    public CustomerPaymentResponse markFailed(Integer paymentId, CustomerPaymentFailureRequest request) {
        CustomerPaymentTransaction payment = getPaymentForUpdate(paymentId);
        payment.fail(request.getReason());
        repository.save(payment);

        eventPublisher.publishEvent(PaymentFailedEvent.now(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getFailureReason()
        ));
        return CustomerPaymentMapper.toResponse(payment);
    }

    @Transactional
    public CustomerPaymentResponse cancel(Integer paymentId, CustomerPaymentCancelRequest request, Integer currentUserId, boolean isAdmin) {
        CustomerPaymentTransaction payment = getPaymentForUpdate(paymentId);
        validateOwnership(payment.getId(), payment.getUserId(), currentUserId, isAdmin, "cancel");
        payment.cancel(request.getReason());
        repository.save(payment);

        eventPublisher.publishEvent(PaymentCancelledEvent.now(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getCancelReason()
        ));
        return CustomerPaymentMapper.toResponse(payment);
    }

    @Transactional
    public void delete(Integer paymentId) {
        CustomerPaymentTransaction payment = getPayment(paymentId);
        if (payment.getStatus() == CustomerPaymentStatus.SUCCEEDED) {
            throw new InvalidCustomerCustomerPaymentOperationException("delete", "Succeeded payment cannot be deleted.");
        }

        int updatedRows = repository.softDeleteWithVersion(
                paymentId,
                payment.getVersion(),
                List.of(CustomerPaymentStatus.FAILED, CustomerPaymentStatus.CANCELLED, CustomerPaymentStatus.EXPIRED)
        );
        if (updatedRows == 0) {
            throw new CustomerCustomerPaymentDeletedConflictException(paymentId);
        }
    }

    @Transactional
    public VnpayReturnResponse handleVnpayReturn(Map<String, String> params) {
        boolean validSignature = vnpayService.isValidSignature(params);
        Integer paymentId = vnpayService.extractPaymentId(params);
        CustomerPaymentTransaction payment = getPayment(paymentId);
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String message = validSignature
                ? resolveVnpayMessage(responseCode, transactionStatus)
                : "VNPay signature is invalid. Do not trust this payment result.";

        if (validSignature && payment.getStatus() == CustomerPaymentStatus.PENDING) {
            String vnpayTxnNo = params.get("vnp_TransactionNo");
            if ("00".equals(responseCode) || "00".equals(transactionStatus)) {
                payment.succeed(vnpayTxnNo == null || vnpayTxnNo.isBlank() ? "VNPAY-" + payment.getId() : vnpayTxnNo);
                repository.save(payment);
                publishPaymentCompleted(payment);
            } else if (responseCode != null && !responseCode.isBlank()) {
                payment.fail(message);
                repository.save(payment);
                publishPaymentFailed(payment);
            }
        }

        return new VnpayReturnResponse(
                validSignature,
                responseCode,
                transactionStatus,
                message,
                CustomerPaymentMapper.toResponse(payment)
        );
    }

    @Transactional
    public VnpayIpnResponse handleVnpayIpn(Map<String, String> params) {
        if (!vnpayService.isValidSignature(params)) {
            return new VnpayIpnResponse("97", "Invalid signature");
        }

        Integer paymentId;
        try {
            paymentId = vnpayService.extractPaymentId(params);
        } catch (RuntimeException ex) {
            return new VnpayIpnResponse("01", "Order not found");
        }

        CustomerPaymentTransaction payment = repository.findByIdForUpdate(paymentId).orElse(null);
        if (payment == null) {
            return new VnpayIpnResponse("01", "Order not found");
        }

        if (vnpayService.toVnpayAmount(payment.getAmount()) != vnpayService.extractAmount(params)) {
            return new VnpayIpnResponse("04", "Invalid amount");
        }

        if (payment.getStatus() == CustomerPaymentStatus.SUCCEEDED) {
            return new VnpayIpnResponse("00", "Order already confirmed");
        }

        if (payment.getStatus() != CustomerPaymentStatus.PENDING) {
            return new VnpayIpnResponse("02", "Order already confirmed");
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            payment.succeed(params.get("vnp_TransactionNo"));
            repository.save(payment);
            publishPaymentCompleted(payment);
        } else {
            payment.fail(resolveVnpayMessage(responseCode, transactionStatus));
            repository.save(payment);
            publishPaymentFailed(payment);
        }

        return new VnpayIpnResponse("00", "Confirm success");
    }

    private CustomerPaymentTransaction getPayment(Integer paymentId) {
        return repository.findById(paymentId).orElseThrow(() -> new CustomerPaymentNotFoundException(paymentId));
    }

    private CustomerPaymentTransaction getPaymentForUpdate(Integer paymentId) {
        return repository.findByIdForUpdate(paymentId).orElseThrow(() -> new CustomerPaymentNotFoundException(paymentId));
    }

    private CustomerPaymentRefund getRefund(Integer refundId) {
        return refundRepository.findById(refundId).orElseThrow(() -> new CustomerPaymentRefundNotFoundException(refundId));
    }

    private CustomerPaymentRefund getRefundForUpdate(Integer refundId) {
        return refundRepository.findByIdForUpdate(refundId).orElseThrow(() -> new CustomerPaymentRefundNotFoundException(refundId));
    }

    private void validateRefundablePayment(CustomerPaymentTransaction payment) {
        if (payment.getStatus() != CustomerPaymentStatus.SUCCEEDED
                && payment.getStatus() != CustomerPaymentStatus.PARTIALLY_REFUNDED) {
            throw new InvalidCustomerCustomerPaymentOperationException(
                    "requestRefund",
                    "Only succeeded or partially refunded payments can be refunded."
            );
        }
    }

    private void completeSuccessfulRefund(CustomerPaymentTransaction payment, CustomerPaymentRefund refund) {
        refundRepository.saveAndFlush(refund);
        double succeededAmount = refundRepository.sumAmountByPaymentIdAndStatuses(
                payment.getId(),
                List.of(RefundStatus.SUCCEEDED)
        );
        boolean fullyRefunded = succeededAmount + 0.005 >= payment.getAmount();
        payment.completeRefund(fullyRefunded);
    }

    private boolean hasPreviousSuccessfulRefund(Integer paymentId) {
        return refundRepository.sumAmountByPaymentIdAndStatuses(
                paymentId,
                List.of(RefundStatus.SUCCEEDED)
        ) > 0;
    }

    private void validateRefundMethod(CustomerPaymentTransaction payment, RefundMethod refundMethod) {
        if (payment.getMethod() == CustomerPaymentMethod.VNPAY && refundMethod != RefundMethod.VNPAY) {
            throw new InvalidCustomerCustomerPaymentOperationException("requestRefund", "VNPay payment must be refunded through VNPay.");
        }
        if (payment.getMethod() == CustomerPaymentMethod.COD && refundMethod != RefundMethod.COD_MANUAL) {
            throw new InvalidCustomerCustomerPaymentOperationException("requestRefund", "COD payment must be refunded manually.");
        }
    }

    private void validateRefundAmount(CustomerPaymentTransaction payment, double requestedAmount) {
        double alreadyRefunded = refundRepository.sumAmountByPaymentIdAndStatuses(
                payment.getId(),
                List.of(RefundStatus.PENDING, RefundStatus.PROCESSING, RefundStatus.SUCCEEDED)
        );
        double refundableAmount = Math.max(0.0, Math.round((payment.getAmount() - alreadyRefunded) * 100.0) / 100.0);
        if (requestedAmount > refundableAmount) {
            throw new DuplicateCustomerCustomerPaymentRefundException(payment.getId(), requestedAmount, refundableAmount);
        }
    }

    private void validateOrderCanBePaid(OrderPaymentSnapshot order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw CustomerCustomerPaymentCrossModuleException.invalidOrder(
                    order.getOrderId(),
                    "only PENDING orders can create a new payment"
            );
        }
        if (order.getPayableAmount() <= 0) {
            throw CustomerCustomerPaymentCrossModuleException.invalidOrder(order.getOrderId(), "payable amount must be greater than 0");
        }
    }

    private void validateNoActivePayment(Integer orderId) {
        if (repository.existsByOrderIdAndStatusIn(orderId, ACTIVE_PAYMENT_STATUSES)) {
            throw new DuplicateActiveCustomerCustomerPaymentException(orderId);
        }
    }

    private void publishPaymentCompleted(CustomerPaymentTransaction payment) {
        OrderPaymentSnapshot order = orderFacade.getPaymentSnapshot(payment.getOrderId());
        eventPublisher.publishEvent(PaymentCompletedEvent.now(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMethod(),
                payment.getAmount(),
                order.getCostAmount(),
                payment.getProviderTransactionId()
        ));
    }

    private void publishPaymentFailed(CustomerPaymentTransaction payment) {
        eventPublisher.publishEvent(PaymentFailedEvent.now(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getFailureReason()
        ));
    }

    private void publishPaymentRefunded(CustomerPaymentRefund refund) {
        eventPublisher.publishEvent(PaymentRefundedEvent.now(
                refund.getId(),
                refund.getPaymentId(),
                refund.getOrderId(),
                refund.getUserId(),
                refund.getMethod(),
                refund.getAmount(),
                refund.getRefundCode(),
                extractCustomerReturnId(refund.getRefundCode())
        ));
    }

    private Integer extractCustomerReturnId(String refundCode) {
        if (refundCode == null || !refundCode.startsWith("REF-RETURN-")) return null;
        String remainder = refundCode.substring("REF-RETURN-".length());
        int separator = remainder.indexOf("-PAY-");
        if (separator <= 0) return null;
        try {
            return Integer.valueOf(remainder.substring(0, separator));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String generateRefundCode(Integer paymentId) {
        String code;
        do {
            code = "REF-" + paymentId + "-" + System.currentTimeMillis();
        } while (refundRepository.existsByRefundCode(code));
        return code;
    }

    private String generateCustomerReturnRefundCode(Integer paymentId, Integer returnId) {
        return "REF-RETURN-" + returnId + "-PAY-" + paymentId;
    }

    private String resolveVnpayMessage(String responseCode, String transactionStatus) {
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            return "VNPay payment was successful.";
        }
        return "VNPay payment was not successful. responseCode=" + responseCode + ", transactionStatus=" + transactionStatus;
    }

    private void validateOwnership(Integer targetId, Integer ownerId, Integer currentUserId, boolean isAdmin, String action) {
        if (!ownerId.equals(currentUserId) && !isAdmin) {
            throw new CustomerCustomerPaymentAccessDeniedException(targetId, currentUserId, action);
        }
    }
}
