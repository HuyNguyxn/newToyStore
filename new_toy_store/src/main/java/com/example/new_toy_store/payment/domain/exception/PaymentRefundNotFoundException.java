package com.example.new_toy_store.payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PaymentRefundNotFoundException extends PaymentDomainException {

    public PaymentRefundNotFoundException(Integer refundId) {
        super(
                HttpStatus.NOT_FOUND,
                "PAYMENT_REFUND_NOT_FOUND",
                "Payment refund was not found.",
                Map.of("refundId", refundId)
        );
    }
}
