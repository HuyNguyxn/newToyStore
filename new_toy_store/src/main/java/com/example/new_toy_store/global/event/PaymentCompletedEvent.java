package com.example.new_toy_store.global.event;

import com.example.new_toy_store.payment.domain.PaymentMethod;

import java.time.Instant;

public record PaymentCompletedEvent(
        Integer paymentId,
        Integer orderId,
        Integer userId,
        PaymentMethod method,
        double amount,
        String providerTransactionId,
        Instant occurredAt
) {
    public static PaymentCompletedEvent now(
            Integer paymentId,
            Integer orderId,
            Integer userId,
            PaymentMethod method,
            double amount,
            String providerTransactionId
    ) {
        return new PaymentCompletedEvent(
                paymentId,
                orderId,
                userId,
                method,
                amount,
                providerTransactionId,
                Instant.now()
        );
    }
}
