package com.example.new_toy_store.promotion.domain;

import com.example.new_toy_store.promotion.domain.exception.InvalidPromotionOperationException;

public enum PromotionScope {
    PRODUCT("Khuyến mãi trên từng sản phẩm") {
        @Override
        public void validateSetup(Double minOrderValue, Integer targetProductId) {
            if (targetProductId == null) {
                throw InvalidPromotionOperationException.missingTargetProduct();
            }
        }
    },
    ORDER("Khuyến mãi trên tổng đơn hàng") {
        @Override
        public void validateSetup(Double minOrderValue, Integer targetProductId) {
            if (targetProductId != null) {
                throw InvalidPromotionOperationException.invalidTargetProductForOrder();
            }
        }
    },
    SHIPPING("Khuyến mãi phí vận chuyển") {
        @Override
        public void validateSetup(Double minOrderValue, Integer targetProductId) {
            if (targetProductId != null) {
                throw InvalidPromotionOperationException.invalidTargetProductForShipping();
            }
        }
    };

    private final String description;

    PromotionScope(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract void validateSetup(Double minOrderValue, Integer targetProductId);

    public static PromotionScope from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidPromotionOperationException.nullScope();
        }
        for (PromotionScope scope : values()) {
            if (scope.name().equalsIgnoreCase(value.trim())) {
                return scope;
            }
        }
        throw InvalidPromotionOperationException.invalidScope(value);
    }
}