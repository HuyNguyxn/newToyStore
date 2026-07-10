package com.example.new_toy_store.order.domain.exception;

public class InvalidOrderDataException extends RuntimeException {
    private final String field;

    public InvalidOrderDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}