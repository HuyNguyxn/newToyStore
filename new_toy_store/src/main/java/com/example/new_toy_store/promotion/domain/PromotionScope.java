package com.example.new_toy_store.promotion.domain;

import com.example.new_toy_store.promotion.domain.exception.InvalidPromotionOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PromotionScope {
    PRODUCT(
            "PRODUCT",
            "Khuyến mãi theo sản phẩm",
            "Áp dụng cho một sản phẩm mục tiêu."
    ) {
        @Override
        public void validateSetup(Double minOrderValue, Integer targetProductId) {
            if (targetProductId == null) {
                throw InvalidPromotionOperationException.missingTargetProduct();
            }
        }
    },
    ORDER(
            "ORDER",
            "Khuyến mãi theo đơn hàng",
            "Áp dụng trên tổng giá trị đơn hàng."
    ) {
        @Override
        public void validateSetup(Double minOrderValue, Integer targetProductId) {
            if (targetProductId != null) {
                throw InvalidPromotionOperationException.invalidTargetProductForOrder();
            }
        }
    },
    SHIPPING(
            "SHIPPING",
            "Khuyến mãi phí vận chuyển",
            "Áp dụng cho phí vận chuyển của đơn hàng."
    ) {
        @Override
        public void validateSetup(Double minOrderValue, Integer targetProductId) {
            if (targetProductId != null) {
                throw InvalidPromotionOperationException.invalidTargetProductForShipping();
            }
        }
    };

    private final String code;
    private final String displayName;
    private final String description;

    PromotionScope(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public abstract void validateSetup(Double minOrderValue, Integer targetProductId);

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PromotionScope from(Object input) {
        if (input == null) {
            throw InvalidPromotionOperationException.nullScope();
        }

        if (input instanceof Map<?, ?> objectValue) {
            Object codeValue = objectValue.get("code");
            if (codeValue == null) {
                codeValue = objectValue.get("name");
            }
            if (codeValue == null) {
                codeValue = objectValue.get("scope");
            }
            return fromText(String.valueOf(codeValue));
        }

        return fromText(String.valueOf(input));
    }

    public static PromotionScope from(String value) {
        return fromText(value);
    }

    private static PromotionScope fromText(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            throw InvalidPromotionOperationException.nullScope();
        }

        String normalized = value.trim().toUpperCase();
        for (PromotionScope scope : values()) {
            if (scope.name().equals(normalized) || scope.code.equalsIgnoreCase(value.trim())) {
                return scope;
            }
        }

        throw InvalidPromotionOperationException.invalidScope(value);
    }
}
