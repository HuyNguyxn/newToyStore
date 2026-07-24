package com.example.new_toy_store.promotion.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidPromotionDataException extends PromotionDomainException {

    public InvalidPromotionDataException(String field, String message) {
        super(
                HttpStatus.BAD_REQUEST,
                "PROMOTION_INVALID_INPUT",
                message,
                Map.of(
                        "field", field,
                        "reason", "INVALID_INPUT"
                )
        );
    }
}
