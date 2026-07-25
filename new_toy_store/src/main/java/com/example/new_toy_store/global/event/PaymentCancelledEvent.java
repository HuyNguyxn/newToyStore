package com.example.new_toy_store.global.event;

import com.example.new_toy_store.payment.domain.PaymentMethod;

import java.time.Instant;

public record PaymentCancelledEvent(
        Integer paymentId,
        Integer orderId,
        Integer userId,
        PaymentMethod method,
        double amount,
        String reason,
        Instant occurredAt
) {
    public static PaymentCancelledEvent now(
            Integer paymentId,
            Integer orderId,
            Integer userId,
            PaymentMethod method,
            double amount,
            String reason
    ) {
        return new PaymentCancelledEvent(paymentId, orderId, userId, method, amount, reason, Instant.now());
    }
}
