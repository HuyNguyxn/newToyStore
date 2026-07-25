package com.example.new_toy_store.payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PaymentRefundDeletedConflictException extends PaymentDomainException {

    public PaymentRefundDeletedConflictException(Integer refundId) {
        super(
                HttpStatus.CONFLICT,
                "PAYMENT_REFUND_DELETED_CONFLICT",
                "Payment refund was already deleted or changed by another request.",
                Map.of("refundId", refundId)
        );
    }
}
