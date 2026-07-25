package com.example.new_toy_store.payment.domain.exception;

import com.example.new_toy_store.payment.domain.PaymentStatus;
import com.example.new_toy_store.payment.domain.RefundStatus;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidPaymentOperationException extends PaymentDomainException {

    public InvalidPaymentOperationException(String action, String reason) {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAYMENT_OPERATION",
                reason,
                Map.of("action", action, "reason", reason)
        );
    }

    public static InvalidPaymentOperationException invalidTransition(
            PaymentStatus currentStatus,
            PaymentStatus nextStatus,
            Integer paymentId
    ) {
        return new InvalidPaymentOperationException(
                "changeStatus",
                "Payment status cannot change from " + currentStatus.name() + " to " + nextStatus.name() + "."
        ).withContext(paymentId, currentStatus, nextStatus);
    }

    public static InvalidPaymentOperationException invalidRefundTransition(
            Integer refundId,
            RefundStatus currentStatus,
            RefundStatus nextStatus
    ) {
        return new InvalidPaymentOperationException(
                "changeRefundStatus",
                "Refund status cannot change from " + currentStatus.name() + " to " + nextStatus.name() + ".",
                Map.of("refundId", refundId, "currentStatus", currentStatus.name(), "nextStatus", nextStatus.name())
        );
    }

    private InvalidPaymentOperationException withContext(
            Integer paymentId,
            PaymentStatus currentStatus,
            PaymentStatus nextStatus
    ) {
        return new InvalidPaymentOperationException(
                "changeStatus",
                "Payment status cannot change from " + currentStatus.name() + " to " + nextStatus.name() + ".",
                Map.of("paymentId", paymentId, "currentStatus", currentStatus.name(), "nextStatus", nextStatus.name())
        );
    }

    private InvalidPaymentOperationException(String action, String reason, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_OPERATION", reason, contextData);
    }
}
