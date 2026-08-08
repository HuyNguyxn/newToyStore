package com.example.new_toy_store.customer_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CustomerPaymentNotFoundException extends CustomerPaymentDomainException {

    public CustomerPaymentNotFoundException(Integer paymentId) {
        super(
                HttpStatus.NOT_FOUND,
                "PAYMENT_NOT_FOUND",
                "Payment transaction was not found.",
                Map.of("paymentId", paymentId)
        );
    }

    public static CustomerPaymentNotFoundException byOrderId(Integer orderId) {
        return new CustomerPaymentNotFoundException("Payment transaction was not found for this order.", Map.of("orderId", orderId));
    }

    private CustomerPaymentNotFoundException(String message, Map<String, Object> contextData) {
        super(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", message, contextData);
    }
}
