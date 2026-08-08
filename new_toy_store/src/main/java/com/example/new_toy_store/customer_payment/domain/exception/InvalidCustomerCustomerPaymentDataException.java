package com.example.new_toy_store.customer_payment.domain.exception;

import com.example.new_toy_store.customer_payment.domain.CustomerPaymentMethod;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Map;

public class InvalidCustomerCustomerPaymentDataException extends CustomerPaymentDomainException {

    public InvalidCustomerCustomerPaymentDataException(String field, String reason) {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAYMENT_DATA",
                reason,
                Map.of("field", field, "reason", reason)
        );
    }

    public static InvalidCustomerCustomerPaymentDataException emptyMethod() {
        return new InvalidCustomerCustomerPaymentDataException("method", "Payment method must not be empty.");
    }

    public static InvalidCustomerCustomerPaymentDataException invalidMethod(String value) {
        return new InvalidCustomerCustomerPaymentDataException(
                "method",
                "Payment method [" + value + "] is invalid. Allowed values: " + Arrays.toString(CustomerPaymentMethod.values())
        );
    }

    public static InvalidCustomerCustomerPaymentDataException unsupportedMethod(CustomerPaymentMethod method) {
        return new InvalidCustomerCustomerPaymentDataException(
                "method",
                "Payment method [" + method.name() + "] is not available."
        );
    }

    public static InvalidCustomerCustomerPaymentDataException emptyStatus() {
        return new InvalidCustomerCustomerPaymentDataException("status", "Payment status must not be empty.");
    }
}
