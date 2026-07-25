package com.example.new_toy_store.payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PaymentDeletedConflictException extends PaymentDomainException {

    public PaymentDeletedConflictException(Integer paymentId) {
        super(
                HttpStatus.CONFLICT,
                "PAYMENT_DELETED_CONFLICT",
                "Payment transaction was already deleted or changed by another request.",
                Map.of("paymentId", paymentId)
        );
    }
}
