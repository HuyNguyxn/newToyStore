package com.example.new_toy_store.customer_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CustomerCustomerPaymentCrossModuleException extends CustomerPaymentDomainException {

    private CustomerCustomerPaymentCrossModuleException(String message, Map<String, Object> contextData) {
        super(HttpStatus.CONFLICT, "PAYMENT_CROSS_MODULE_ERROR", message, contextData);
    }

    public static CustomerCustomerPaymentCrossModuleException invalidOrder(Integer orderId, String reason) {
        return new CustomerCustomerPaymentCrossModuleException(
                "Cannot create payment for this order: " + reason,
                Map.of("orderId", orderId, "reason", reason)
        );
    }
}
