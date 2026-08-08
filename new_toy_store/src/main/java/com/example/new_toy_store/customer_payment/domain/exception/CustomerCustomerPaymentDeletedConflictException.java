package com.example.new_toy_store.customer_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CustomerCustomerPaymentDeletedConflictException extends CustomerPaymentDomainException {

    public CustomerCustomerPaymentDeletedConflictException(Integer paymentId) {
        super(
                HttpStatus.CONFLICT,
                "PAYMENT_DELETED_CONFLICT",
                "Payment transaction was already deleted or changed by another request.",
                Map.of("paymentId", paymentId)
        );
    }
}
