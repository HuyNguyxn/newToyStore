package com.example.new_toy_store.payment.application;

import com.example.new_toy_store.global.event.PaymentCancelledEvent;
import com.example.new_toy_store.global.event.PaymentCompletedEvent;
import com.example.new_toy_store.global.event.PaymentFailedEvent;
import com.example.new_toy_store.infrastructure.specification.PaymentSpecification;
import com.example.new_toy_store.order.application.dto.response.OrderPaymentSnapshot;
import com.example.new_toy_store.order.application.facade.OrderFacade;
import com.example.new_toy_store.order.domain.OrderStatus;
import com.example.new_toy_store.payment.application.dto.request.PaymentCancelRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentCheckoutRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentConfirmRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentFailureRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentFilterRequest;
import com.example.new_toy_store.payment.application.dto.response.PaymentResponse;
import com.example.new_toy_store.payment.application.dto.response.VnpayReturnResponse;
import com.example.new_toy_store.payment.domain.PaymentMethod;
import com.example.new_toy_store.payment.domain.PaymentRepository;
import com.example.new_toy_store.payment.domain.PaymentStatus;
import com.example.new_toy_store.payment.domain.PaymentTransaction;
import com.example.new_toy_store.payment.domain.exception.DuplicateActivePaymentException;
import com.example.new_toy_store.payment.domain.exception.InvalidPaymentOperationException;
import com.example.new_toy_store.payment.domain.exception.PaymentAccessDeniedException;
import com.example.new_toy_store.payment.domain.exception.PaymentCrossModuleException;
import com.example.new_toy_store.payment.domain.exception.PaymentDeletedConflictException;
import com.example.new_toy_store.payment.domain.exception.PaymentNotFoundException;
import com.example.new_toy_store.payment.infrastructure.vnpay.VnpayIpnResponse;
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
    private final OrderFacade orderFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final VnpayService vnpayService;

    public PaymentService(
            PaymentRepository repository,
            OrderFacade orderFacade,
            ApplicationEventPublisher eventPublisher,
            VnpayService vnpayService
    ) {
        this.repository = repository;
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
