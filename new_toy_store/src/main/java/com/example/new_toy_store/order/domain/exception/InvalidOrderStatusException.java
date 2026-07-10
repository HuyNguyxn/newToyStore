package com.example.new_toy_store.order.domain.exception;

import java.util.List;

public class InvalidOrderStatusException extends RuntimeException {
    private final String invalidValue;
    private final List<String> allowedValues;

    public InvalidOrderStatusException(String invalidValue, List<String> allowedValues) {
        super(String.format("Trạng thái '%s' không hợp lệ. Cho phép: %s", invalidValue, allowedValues));
        this.invalidValue = invalidValue;
        this.allowedValues = allowedValues;
    }

    public String getInvalidValue() { return invalidValue; }
    public List<String> getAllowedValues() { return allowedValues; }
}