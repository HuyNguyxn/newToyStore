package com.example.new_toy_store.payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class InvalidPaymentStatusException extends PaymentDomainException {

    private InvalidPaymentStatusException(String message, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_STATUS", message, contextData);
    }

    public static InvalidPaymentStatusException emptyStatus() {
        return new InvalidPaymentStatusException(
                "Payment status must not be empty.",
                Map.of("field", "status")
        );
    }

    public static InvalidPaymentStatusException invalidStatus(String value, List<String> allowedStatuses) {
        return new InvalidPaymentStatusException(
                "Payment status [" + value + "] is invalid.",
                Map.of("value", value, "allowedStatuses", allowedStatuses)
        );
    }

    public static InvalidPaymentStatusException emptyRefundStatus() {
        return new InvalidPaymentStatusException(
                "Refund status must not be empty.",
                Map.of("field", "refundStatus")
        );
    }

    public static InvalidPaymentStatusException invalidRefundStatus(String value, List<String> allowedStatuses) {
        return new InvalidPaymentStatusException(
                "Refund status [" + value + "] is invalid.",
                Map.of("value", value, "allowedStatuses", allowedStatuses)
        );
    }
}
