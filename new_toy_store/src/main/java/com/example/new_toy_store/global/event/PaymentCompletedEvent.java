package com.example.new_toy_store.global.event;

import com.example.new_toy_store.customer_payment.domain.CustomerPaymentMethod;

import java.time.Instant;

public record PaymentCompletedEvent(
        Integer paymentId,
        Integer orderId,
        Integer userId,
        CustomerPaymentMethod method,
        double amount,
        double costAmount,
        String providerTransactionId,
        Instant occurredAt
) {
    public static PaymentCompletedEvent now(
            Integer paymentId,
            Integer orderId,
            Integer userId,
            CustomerPaymentMethod method,
            double amount,
            double costAmount,
            String providerTransactionId
    ) {
        return new PaymentCompletedEvent(
                paymentId,
                orderId,
                userId,
                method,
                amount,
                costAmount,
                providerTransactionId,
                Instant.now()
        );
    }
}
