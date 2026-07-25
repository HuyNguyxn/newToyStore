package com.example.new_toy_store.global.event;

import com.example.new_toy_store.payment.domain.PaymentMethod;

import java.time.Instant;

public record PaymentFailedEvent(
        Integer paymentId,
        Integer orderId,
        Integer userId,
        PaymentMethod method,
        double amount,
        String reason,
        Instant occurredAt
) {
    public static PaymentFailedEvent now(
            Integer paymentId,
            Integer orderId,
            Integer userId,
            PaymentMethod method,
            double amount,
            String reason
    ) {
        return new PaymentFailedEvent(paymentId, orderId, userId, method, amount, reason, Instant.now());
    }
}
