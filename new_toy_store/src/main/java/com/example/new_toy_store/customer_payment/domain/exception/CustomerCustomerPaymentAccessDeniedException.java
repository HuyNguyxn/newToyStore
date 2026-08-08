package com.example.new_toy_store.customer_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CustomerCustomerPaymentAccessDeniedException extends CustomerPaymentDomainException {

    public CustomerCustomerPaymentAccessDeniedException(Integer paymentId, Integer userId, String action) {
        super(
                HttpStatus.FORBIDDEN,
                "PAYMENT_ACCESS_DENIED",
                "You do not have permission to " + action + " this payment transaction.",
                Map.of("paymentId", paymentId, "userId", userId, "action", action)
        );
    }
}
