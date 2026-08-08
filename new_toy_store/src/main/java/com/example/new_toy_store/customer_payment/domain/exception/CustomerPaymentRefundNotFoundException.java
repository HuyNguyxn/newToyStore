package com.example.new_toy_store.customer_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CustomerPaymentRefundNotFoundException extends CustomerPaymentDomainException {

    public CustomerPaymentRefundNotFoundException(Integer refundId) {
        super(
                HttpStatus.NOT_FOUND,
                "PAYMENT_REFUND_NOT_FOUND",
                "Payment refund was not found.",
                Map.of("refundId", refundId)
        );
    }
}
