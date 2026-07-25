package com.example.new_toy_store.payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PaymentCrossModuleException extends PaymentDomainException {

    private PaymentCrossModuleException(String message, Map<String, Object> contextData) {
        super(HttpStatus.CONFLICT, "PAYMENT_CROSS_MODULE_ERROR", message, contextData);
    }

    public static PaymentCrossModuleException invalidOrder(Integer orderId, String reason) {
        return new PaymentCrossModuleException(
                "Cannot create payment for this order: " + reason,
                Map.of("orderId", orderId, "reason", reason)
        );
    }
}
