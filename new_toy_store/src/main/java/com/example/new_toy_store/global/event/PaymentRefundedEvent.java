package com.example.new_toy_store.global.event;

import com.example.new_toy_store.customer_payment.domain.RefundMethod;

import java.time.Instant;

public record PaymentRefundedEvent(
        Integer refundId,
        Integer paymentId,
        Integer orderId,
        Integer userId,
        RefundMethod method,
        double amount,
        String refundCode,
        Integer customerReturnId,
        Instant occurredAt
) {
    public static PaymentRefundedEvent now(
            Integer refundId,
            Integer paymentId,
            Integer orderId,
            Integer userId,
            RefundMethod method,
            double amount,
            String refundCode,
            Integer customerReturnId
    ) {
        return new PaymentRefundedEvent(
                refundId, paymentId, orderId, userId, method, amount, refundCode, customerReturnId, Instant.now()
        );
    }
}
