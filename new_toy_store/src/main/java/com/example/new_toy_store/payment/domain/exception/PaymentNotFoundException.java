package com.example.new_toy_store.payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PaymentNotFoundException extends PaymentDomainException {

    public PaymentNotFoundException(Integer paymentId) {
        super(
                HttpStatus.NOT_FOUND,
                "PAYMENT_NOT_FOUND",
                "Payment transaction was not found.",
                Map.of("paymentId", paymentId)
        );
    }

    public static PaymentNotFoundException byOrderId(Integer orderId) {
        return new PaymentNotFoundException("Payment transaction was not found for this order.", Map.of("orderId", orderId));
    }

    private PaymentNotFoundException(String message, Map<String, Object> contextData) {
        super(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", message, contextData);
    }
}
