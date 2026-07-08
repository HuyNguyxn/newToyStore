package com.example.new_toy_store.promotion.domain.exception;

public class PromotionNotFoundException extends RuntimeException {

    private final Object identifier;

    public PromotionNotFoundException(Object identifier) {
        super("Không tìm thấy chương trình khuyến mãi (Identifier: " + identifier + ").");
        this.identifier = identifier;
    }

    public Object getIdentifier() {
        return identifier;
    }
}