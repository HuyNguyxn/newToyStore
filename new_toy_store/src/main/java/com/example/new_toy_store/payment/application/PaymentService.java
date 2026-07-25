package com.example.new_toy_store.payment.application;

import com.example.new_toy_store.global.event.PaymentCancelledEvent;
import com.example.new_toy_store.global.event.PaymentCompletedEvent;
import com.example.new_toy_store.global.event.PaymentFailedEvent;
import com.example.new_toy_store.global.event.PaymentRefundedEvent;
import com.example.new_toy_store.infrastructure.specification.PaymentSpecification;
import com.example.new_toy_store.order.application.dto.response.OrderPaymentSnapshot;
import com.example.new_toy_store.order.application.facade.OrderFacade;
import com.example.new_toy_store.order.domain.OrderStatus;
import com.example.new_toy_store.payment.application.dto.request.PaymentCancelRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentCheckoutRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentConfirmRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentFailureRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentFilterRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentRefundDecisionRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentRefundRequest;
import com.example.new_toy_store.payment.application.dto.response.PaymentRefundResponse;
import com.example.new_toy_store.payment.application.dto.response.PaymentResponse;
import com.example.new_toy_store.payment.application.dto.response.VnpayReturnResponse;
import com.example.new_toy_store.payment.domain.PaymentMethod;
import com.example.new_toy_store.payment.domain.PaymentRefund;
import com.example.new_toy_store.payment.domain.PaymentRefundRepository;
import com.example.new_toy_store.payment.domain.PaymentRepository;
import com.example.new_toy_store.payment.domain.PaymentStatus;
import com.example.new_toy_store.payment.domain.PaymentTransaction;
import com.example.new_toy_store.payment.domain.RefundMethod;
import com.example.new_toy_store.payment.domain.RefundStatus;
import com.example.new_toy_store.payment.domain.exception.DuplicateActivePaymentException;
import com.example.new_toy_store.payment.domain.exception.DuplicatePaymentRefundException;
import com.example.new_toy_store.payment.domain.exception.InvalidPaymentOperationException;
import com.example.new_toy_store.payment.domain.exception.PaymentAccessDeniedException;
import com.example.new_toy_store.payment.domain.exception.PaymentCrossModuleException;
import com.example.new_toy_store.payment.domain.exception.PaymentDeletedConflictException;
import com.example.new_toy_store.payment.domain.exception.PaymentNotFoundException;
import com.example.new_toy_store.payment.domain.exception.PaymentRefundDeletedConflictException;
import com.example.new_toy_store.payment.domain.exception.PaymentRefundNotFoundException;
import com.example.new_toy_store.payment.infrastructure.vnpay.VnpayIpnResponse;
import com.example.new_toy_store.payment.infrastructure.vnpay.VnpayRefundResponse;
import com.example.new_toy_store.payment.infrastructure.vnpay.VnpayService;
import com.example.new_toy_store.payment.mapper.PaymentMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final List<PaymentStatus> ACTIVE_PAYMENT_STATUSES = List.of(PaymentStatus.PENDING, PaymentStatus.SUCCEEDED);

    private final PaymentRepository repository;
    private final PaymentRefundRepository refundRepository;
    private final OrderFacade orderFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final VnpayService vnpayService;

    public PaymentService(
            PaymentRepository repository,
            PaymentRefundRepository refundRepository,
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
    public PaymentResponse checkout(PaymentCheckoutRequest request, Integer currentUserId, boolean isAdmin, String clientIp) {
        OrderPaymentSnapshot order = orderFacade.getPaymentSnapshot(request.getOrderId());
        validateOrderCanBePaid(order);
        validateOwnership(order.getOrderId(), order.getUserId(), currentUserId, isAdmin, "checkout");
        validateNoActivePayment(order.getOrderId());

        PaymentTransaction payment = new PaymentTransaction(
                order.getOrderId(),
                order.getUserId(),
                request.getMethod(),
                order.getPayableAmount()
        );

        repository.save(payment);
        if (payment.getMethod() == PaymentMethod.VNPAY) {
            String paymentUrl = vnpayService.createPaymentUrl(payment, clientIp);
            return PaymentMapper.toCheckoutResponse(payment, paymentUrl, "Open this paymentUrl to pay with VNPay sandbox.");
        }
        return PaymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getDetails(Integer paymentId, Integer currentUserId, boolean isAdmin) {
        PaymentTransaction payment = getPayment(paymentId);
        validateOwnership(payment.getId(), payment.getUserId(), currentUserId, isAdmin, "view");
        return PaymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(Integer userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(PaymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> filter(PaymentFilterRequest request, Pageable pageable) {
        Specification<PaymentTransaction> spec = PaymentSpecification.filter(request);
        return repository.findAll(spec, pageable).map(PaymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public boolean hasSucceededPaymentForOrder(Integer orderId) {
        return repository.existsByOrderIdAndStatusIn(orderId, List.of(PaymentStatus.SUCCEEDED));
    }

    @Transactional
    public PaymentRefundResponse requestRefund(Integer paymentId, PaymentRefundRequest request) {
        PaymentTransaction payment = getPaymentForUpdate(paymentId);
        validateRefundablePayment(payment);
        validateRefundMethod(payment, request.getMethod());
        validateRefundAmount(payment, request.getAmount());

        PaymentRefund refund = new PaymentRefund(
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
        return PaymentMapper.toRefundResponse(refund);
    }

    @Transactional
    public PaymentRefundResponse processRefund(Integer refundId, String adminEmail, String clientIp) {
        PaymentRefund refund = getRefundForUpdate(refundId);
        PaymentTransaction payment = getPaymentForUpdate(refund.getPaymentId());

        if (payment.getStatus() == PaymentStatus.REFUND_FAILED) {
            payment.requestRefund();
        }
        refund.startProcessing();
        if (refund.getMethod() == RefundMethod.VNPAY) {
            VnpayRefundResponse gatewayResponse = vnpayService.requestRefund(payment, refund, adminEmail, clientIp);
            if (gatewayResponse.isSuccess()) {
                refund.succeed(gatewayResponse.getProviderRefundId());
                payment.completeRefund();
                publishPaymentRefunded(refund);
            } else {
                refund.fail(gatewayResponse.getMessage());
                payment.failRefund();
            }
        } else {
            refund.succeed(refund.getRefundCode());
            payment.completeRefund();
            publishPaymentRefunded(refund);
        }

        refundRepository.save(refund);
        repository.save(payment);
        return PaymentMapper.toRefundResponse(refund);
    }

    @Transactional
    public PaymentRefundResponse rejectRefund(Integer refundId, PaymentRefundDecisionRequest request) {
        PaymentRefund refund = getRefundForUpdate(refundId);
        PaymentTransaction payment = getPaymentForUpdate(refund.getPaymentId());
        refund.reject(request.getReason());
        payment.failRefund();
        refundRepository.save(refund);
        repository.save(payment);
        return PaymentMapper.toRefundResponse(refund);
    }

    @Transactional(readOnly = true)
    public Page<PaymentRefundResponse> getRefunds(Integer paymentId, Pageable pageable) {
        return refundRepository.findByPaymentId(paymentId, pageable).map(PaymentMapper::toRefundResponse);
    }

    @Transactional
    public void deleteRefund(Integer refundId) {
        PaymentRefund refund = getRefund(refundId);
        int updatedRows = refundRepository.softDeleteWithVersion(
                refundId,
                refund.getVersion(),
                List.of(RefundStatus.REJECTED, RefundStatus.CANCELLED, RefundStatus.FAILED)
        );
        if (updatedRows == 0) {
            throw new PaymentRefundDeletedConflictException(refundId);
        }
    }

    @Transactional
    public PaymentResponse markSucceeded(Integer paymentId, PaymentConfirmRequest request) {
        PaymentTransaction payment = getPaymentForUpdate(paymentId);
        payment.succeed(request == null ? null : request.getProviderTransactionId());
        repository.save(payment);

        eventPublisher.publishEvent(PaymentCompletedEvent.now(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getProviderTransactionId()
        ));
        return PaymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse markFailed(Integer paymentId, PaymentFailureRequest request) {
        PaymentTransaction payment = getPaymentForUpdate(paymentId);
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
        return PaymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse cancel(Integer paymentId, PaymentCancelRequest request, Integer currentUserId, boolean isAdmin) {
        PaymentTransaction payment = getPaymentForUpdate(paymentId);
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
        return PaymentMapper.toResponse(payment);
    }

    @Transactional
    public void delete(Integer paymentId) {
        PaymentTransaction payment = getPayment(paymentId);
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            throw new InvalidPaymentOperationException("delete", "Succeeded payment cannot be deleted.");
        }

        int updatedRows = repository.softDeleteWithVersion(
                paymentId,
                payment.getVersion(),
                List.of(PaymentStatus.FAILED, PaymentStatus.CANCELLED, PaymentStatus.EXPIRED)
        );
        if (updatedRows == 0) {
            throw new PaymentDeletedConflictException(paymentId);
        }
    }

    @Transactional(readOnly = true)
    public VnpayReturnResponse handleVnpayReturn(Map<String, String> params) {
        boolean validSignature = vnpayService.isValidSignature(params);
        Integer paymentId = vnpayService.extractPaymentId(params);
        PaymentTransaction payment = getPayment(paymentId);
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String message = validSignature
                ? resolveVnpayMessage(responseCode, transactionStatus)
                : "VNPay signature is invalid. Do not trust this payment result.";

        return new VnpayReturnResponse(
                validSignature,
                responseCode,
                transactionStatus,
                message,
                PaymentMapper.toResponse(payment)
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

        PaymentTransaction payment = repository.findByIdForUpdate(paymentId).orElse(null);
        if (payment == null) {
            return new VnpayIpnResponse("01", "Order not found");
        }

        if (vnpayService.toVnpayAmount(payment.getAmount()) != vnpayService.extractAmount(params)) {
            return new VnpayIpnResponse("04", "Invalid amount");
        }

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return new VnpayIpnResponse("00", "Order already confirmed");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
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

    private PaymentTransaction getPayment(Integer paymentId) {
        return repository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private PaymentTransaction getPaymentForUpdate(Integer paymentId) {
        return repository.findByIdForUpdate(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private PaymentRefund getRefund(Integer refundId) {
        return refundRepository.findById(refundId).orElseThrow(() -> new PaymentRefundNotFoundException(refundId));
    }

    private PaymentRefund getRefundForUpdate(Integer refundId) {
        return refundRepository.findByIdForUpdate(refundId).orElseThrow(() -> new PaymentRefundNotFoundException(refundId));
    }

    private void validateRefundablePayment(PaymentTransaction payment) {
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new InvalidPaymentOperationException("requestRefund", "Only succeeded payments can be refunded.");
        }
    }

    private void validateRefundMethod(PaymentTransaction payment, RefundMethod refundMethod) {
        if (payment.getMethod() == PaymentMethod.VNPAY && refundMethod != RefundMethod.VNPAY) {
            throw new InvalidPaymentOperationException("requestRefund", "VNPay payment must be refunded through VNPay.");
        }
        if (payment.getMethod() == PaymentMethod.COD && refundMethod != RefundMethod.COD_MANUAL) {
            throw new InvalidPaymentOperationException("requestRefund", "COD payment must be refunded manually.");
        }
    }

    private void validateRefundAmount(PaymentTransaction payment, double requestedAmount) {
        double alreadyRefunded = refundRepository.sumAmountByPaymentIdAndStatuses(
                payment.getId(),
                List.of(RefundStatus.PENDING, RefundStatus.PROCESSING, RefundStatus.SUCCEEDED)
        );
        double refundableAmount = Math.max(0.0, Math.round((payment.getAmount() - alreadyRefunded) * 100.0) / 100.0);
        if (requestedAmount > refundableAmount) {
            throw new DuplicatePaymentRefundException(payment.getId(), requestedAmount, refundableAmount);
        }
    }

    private void validateOrderCanBePaid(OrderPaymentSnapshot order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw PaymentCrossModuleException.invalidOrder(
                    order.getOrderId(),
                    "only PENDING orders can create a new payment"
            );
        }
        if (order.getPayableAmount() <= 0) {
            throw PaymentCrossModuleException.invalidOrder(order.getOrderId(), "payable amount must be greater than 0");
        }
    }

    private void validateNoActivePayment(Integer orderId) {
        if (repository.existsByOrderIdAndStatusIn(orderId, ACTIVE_PAYMENT_STATUSES)) {
            throw new DuplicateActivePaymentException(orderId);
        }
    }

    private void publishPaymentCompleted(PaymentTransaction payment) {
        eventPublisher.publishEvent(PaymentCompletedEvent.now(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getProviderTransactionId()
        ));
    }

    private void publishPaymentFailed(PaymentTransaction payment) {
        eventPublisher.publishEvent(PaymentFailedEvent.now(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getFailureReason()
        ));
    }

    private void publishPaymentRefunded(PaymentRefund refund) {
        eventPublisher.publishEvent(PaymentRefundedEvent.now(
                refund.getId(),
                refund.getPaymentId(),
                refund.getOrderId(),
                refund.getUserId(),
                refund.getMethod(),
                refund.getAmount(),
                refund.getRefundCode()
        ));
    }

    private String generateRefundCode(Integer paymentId) {
        String code;
        do {
            code = "REF-" + paymentId + "-" + System.currentTimeMillis();
        } while (refundRepository.existsByRefundCode(code));
        return code;
    }

    private String resolveVnpayMessage(String responseCode, String transactionStatus) {
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            return "VNPay payment was successful.";
        }
        return "VNPay payment was not successful. responseCode=" + responseCode + ", transactionStatus=" + transactionStatus;
    }

    private void validateOwnership(Integer targetId, Integer ownerId, Integer currentUserId, boolean isAdmin, String action) {
        if (!ownerId.equals(currentUserId) && !isAdmin) {
            throw new PaymentAccessDeniedException(targetId, currentUserId, action);
        }
    }
}
