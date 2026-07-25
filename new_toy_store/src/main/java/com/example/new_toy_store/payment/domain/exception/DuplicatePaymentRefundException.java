package com.example.new_toy_store.payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicatePaymentRefundException extends PaymentDomainException {

    public DuplicatePaymentRefundException(Integer paymentId, double requestedAmount, double refundableAmount) {
        super(
                HttpStatus.CONFLICT,
                "DUPLICATE_OR_EXCESSIVE_REFUND",
                "Refund amount exceeds the remaining refundable payment amount.",
                Map.of("paymentId", paymentId, "requestedAmount", requestedAmount, "refundableAmount", refundableAmount)
        );
    }
}
