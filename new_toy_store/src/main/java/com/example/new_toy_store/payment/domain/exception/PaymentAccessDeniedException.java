package com.example.new_toy_store.payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PaymentAccessDeniedException extends PaymentDomainException {

    public PaymentAccessDeniedException(Integer paymentId, Integer userId, String action) {
        super(
                HttpStatus.FORBIDDEN,
                "PAYMENT_ACCESS_DENIED",
                "You do not have permission to " + action + " this payment transaction.",
                Map.of("paymentId", paymentId, "userId", userId, "action", action)
        );
    }
}
