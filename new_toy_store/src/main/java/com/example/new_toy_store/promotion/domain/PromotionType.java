package com.example.new_toy_store.promotion.domain;

public enum PromotionType {
    PERCENTAGE("Giảm theo phần trăm (%)") {
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
    FIXED_AMOUNT("Giảm số tiền cố định") {
        @Override
        public double calculateDiscount(double amount, double discountValue, Double maxDiscountAmount) {
            if (amount <= 0 || discountValue <= 0) {
                return 0.0;
            }
            double calculated = Math.round(discountValue);
            return Math.max(0.0, Math.min(amount, calculated));
        }
    };

    private final String description;

    PromotionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract double calculateDiscount(double amount, double discountValue, Double maxDiscountAmount);

    public static PromotionType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại khuyến mãi không được để trống");
        }
        try {
            return PromotionType.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Loại khuyến mãi không hợp lệ: " + value);
        }
    }
}