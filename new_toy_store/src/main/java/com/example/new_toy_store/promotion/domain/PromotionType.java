package com.example.new_toy_store.promotion.domain;

import com.example.new_toy_store.promotion.domain.exception.InvalidPromotionOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PromotionType {
    PERCENTAGE(
            "PERCENTAGE",
            "Giảm theo phần trăm (%)",
            "Giảm theo tỷ lệ phần trăm trên số tiền gốc."
    ) {
        @Override
        public double calculateDiscount(double amount, double discountValue, Double maxDiscountAmount) {
            if (amount <= 0 || discountValue <= 0) {
                return 0.0;
            }
            double calculated = Math.round(amount * (discountValue / 100.0));
            if (maxDiscountAmount != null && maxDiscountAmount > 0) {
                calculated = Math.min(calculated, Math.round(maxDiscountAmount));
            }
            return Math.max(0.0, Math.min(amount, calculated));
        }
    },
    FIXED_AMOUNT(
            "FIXED_AMOUNT",
            "Giảm số tiền cố định",
            "Giảm trực tiếp một số tiền cố định trên số tiền gốc."
    ) {
        @Override
        public double calculateDiscount(double amount, double discountValue, Double maxDiscountAmount) {
            if (amount <= 0 || discountValue <= 0) {
                return 0.0;
            }
            double calculated = Math.round(discountValue);
            return Math.max(0.0, Math.min(amount, calculated));
        }
    };

    private final String code;
    private final String displayName;
    private final String description;

    PromotionType(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public abstract double calculateDiscount(double amount, double discountValue, Double maxDiscountAmount);

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
    public static PromotionType from(Object input) {
        if (input == null) {
            throw InvalidPromotionOperationException.nullType();
        }

        if (input instanceof Map<?, ?> objectValue) {
            Object codeValue = objectValue.get("code");
            if (codeValue == null) {
                codeValue = objectValue.get("name");
            }
            if (codeValue == null) {
                codeValue = objectValue.get("type");
            }
            return fromText(String.valueOf(codeValue));
        }

        return fromText(String.valueOf(input));
    }

    public static PromotionType from(String value) {
        return fromText(value);
    }

    private static PromotionType fromText(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            throw InvalidPromotionOperationException.nullType();
        }

        String normalized = value.trim().toUpperCase();
        for (PromotionType type : values()) {
            if (type.name().equals(normalized) || type.code.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw InvalidPromotionOperationException.invalidType(value);
    }
}
