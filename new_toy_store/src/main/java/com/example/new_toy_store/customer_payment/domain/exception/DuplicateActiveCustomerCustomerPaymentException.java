package com.example.new_toy_store.customer_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateActiveCustomerCustomerPaymentException extends CustomerPaymentDomainException {

    public DuplicateActiveCustomerCustomerPaymentException(Integer orderId) {
        super(
                HttpStatus.CONFLICT,
                "DUPLICATE_ACTIVE_PAYMENT",
                "This order already has an active payment transaction.",
                Map.of("orderId", orderId)
        );
    }
}
