package com.example.new_toy_store.order.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class InvalidOrderStatusException extends OrderDomainException {

    private final String invalidValue;
    private final List<String> allowedValues;

    public InvalidOrderStatusException(String invalidValue, List<String> allowedValues) {
        super(
                HttpStatus.BAD_REQUEST,
                "ORDER_INVALID_STATUS",
                "Trạng thái đơn hàng không hợp lệ: " + invalidValue + ".",
                Map.of(
                        "field", "status",
                        "invalidValue", invalidValue,
                        "allowedValues", allowedValues
                )
        );
        this.invalidValue = invalidValue;
        this.allowedValues = allowedValues;
    }

    public String getInvalidValue() { return invalidValue; }
    public List<String> getAllowedValues() { return allowedValues; }
}
