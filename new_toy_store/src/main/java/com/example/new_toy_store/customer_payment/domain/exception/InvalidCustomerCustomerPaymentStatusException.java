package com.example.new_toy_store.customer_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class InvalidCustomerCustomerPaymentStatusException extends CustomerPaymentDomainException {

    private InvalidCustomerCustomerPaymentStatusException(String message, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_STATUS", message, contextData);
    }

    public static InvalidCustomerCustomerPaymentStatusException emptyStatus() {
        return new InvalidCustomerCustomerPaymentStatusException(
                "Payment status must not be empty.",
                Map.of("field", "status")
        );
    }

    public static InvalidCustomerCustomerPaymentStatusException invalidStatus(String value, List<String> allowedStatuses) {
        return new InvalidCustomerCustomerPaymentStatusException(
                "Payment status [" + value + "] is invalid.",
                Map.of("value", value, "allowedStatuses", allowedStatuses)
        );
    }

    public static InvalidCustomerCustomerPaymentStatusException emptyRefundStatus() {
        return new InvalidCustomerCustomerPaymentStatusException(
                "Refund status must not be empty.",
                Map.of("field", "refundStatus")
        );
    }

    public static InvalidCustomerCustomerPaymentStatusException invalidRefundStatus(String value, List<String> allowedStatuses) {
        return new InvalidCustomerCustomerPaymentStatusException(
                "Refund status [" + value + "] is invalid.",
                Map.of("value", value, "allowedStatuses", allowedStatuses)
        );
    }
}
