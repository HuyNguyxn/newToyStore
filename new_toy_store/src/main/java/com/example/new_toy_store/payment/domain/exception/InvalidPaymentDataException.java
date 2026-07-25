package com.example.new_toy_store.payment.domain.exception;

import com.example.new_toy_store.payment.domain.PaymentMethod;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Map;

public class InvalidPaymentDataException extends PaymentDomainException {

    public InvalidPaymentDataException(String field, String reason) {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAYMENT_DATA",
                reason,
                Map.of("field", field, "reason", reason)
        );
    }

    public static InvalidPaymentDataException emptyMethod() {
        return new InvalidPaymentDataException("method", "Payment method must not be empty.");
    }

    public static InvalidPaymentDataException invalidMethod(String value) {
        return new InvalidPaymentDataException(
                "method",
                "Payment method [" + value + "] is invalid. Allowed values: " + Arrays.toString(PaymentMethod.values())
        );
    }

    public static InvalidPaymentDataException unsupportedMethod(PaymentMethod method) {
        return new InvalidPaymentDataException(
                "method",
                "Payment method [" + method.name() + "] is not available yet. VNPay will be integrated in a separate phase."
        );
    }

    public static InvalidPaymentDataException emptyStatus() {
        return new InvalidPaymentDataException("status", "Payment status must not be empty.");
    }
}
