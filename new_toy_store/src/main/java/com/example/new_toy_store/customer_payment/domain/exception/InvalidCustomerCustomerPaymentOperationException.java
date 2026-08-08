package com.example.new_toy_store.customer_payment.domain.exception;

import com.example.new_toy_store.customer_payment.domain.CustomerPaymentStatus;
import com.example.new_toy_store.customer_payment.domain.RefundStatus;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidCustomerCustomerPaymentOperationException extends CustomerPaymentDomainException {

    public InvalidCustomerCustomerPaymentOperationException(String action, String reason) {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAYMENT_OPERATION",
                reason,
                Map.of("action", action, "reason", reason)
        );
    }

    public static InvalidCustomerCustomerPaymentOperationException invalidTransition(
            CustomerPaymentStatus currentStatus,
            CustomerPaymentStatus nextStatus,
            Integer paymentId
    ) {
        return new InvalidCustomerCustomerPaymentOperationException(
                "changeStatus",
                "Payment status cannot change from " + currentStatus.name() + " to " + nextStatus.name() + "."
        ).withContext(paymentId, currentStatus, nextStatus);
    }

    public static InvalidCustomerCustomerPaymentOperationException invalidRefundTransition(
            Integer refundId,
            RefundStatus currentStatus,
            RefundStatus nextStatus
    ) {
        return new InvalidCustomerCustomerPaymentOperationException(
                "changeRefundStatus",
                "Refund status cannot change from " + currentStatus.name() + " to " + nextStatus.name() + ".",
                Map.of("refundId", refundId, "currentStatus", currentStatus.name(), "nextStatus", nextStatus.name())
        );
    }

    private InvalidCustomerCustomerPaymentOperationException withContext(
            Integer paymentId,
            CustomerPaymentStatus currentStatus,
            CustomerPaymentStatus nextStatus
    ) {
        return new InvalidCustomerCustomerPaymentOperationException(
                "changeStatus",
                "Payment status cannot change from " + currentStatus.name() + " to " + nextStatus.name() + ".",
                Map.of("paymentId", paymentId, "currentStatus", currentStatus.name(), "nextStatus", nextStatus.name())
        );
    }

    private InvalidCustomerCustomerPaymentOperationException(String action, String reason, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_OPERATION", reason, contextData);
    }
}
