package com.example.new_toy_store.order.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidOrderDataException extends OrderDomainException {

    private final String field;

    public InvalidOrderDataException(String field, String message) {
        super(
                HttpStatus.BAD_REQUEST,
                "ORDER_INVALID_INPUT",
                message,
                Map.of(
                        "field", field,
                        "reason", "INVALID_INPUT"
                )
        );
        this.field = field;
    }

    public String getField() { return field; }
}
