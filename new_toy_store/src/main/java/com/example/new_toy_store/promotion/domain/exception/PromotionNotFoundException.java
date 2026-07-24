package com.example.new_toy_store.promotion.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PromotionNotFoundException extends PromotionDomainException {

    private final Object identifier;

    public PromotionNotFoundException(Object identifier) {
        super(
                HttpStatus.NOT_FOUND,
                "PROMOTION_NOT_FOUND",
                "Không tìm thấy chương trình khuyến mãi: " + identifier + ".",
                Map.of(
                        "identifier", String.valueOf(identifier),
                        "entity", "Promotion"
                )
        );
        this.identifier = identifier;
    }

    public Object getIdentifier() {
        return identifier;
    }
}
