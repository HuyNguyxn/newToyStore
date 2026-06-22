package com.example.new_toy_store.promotion.domain;

public enum PromotionScope {
    PRODUCT("Khuyến mãi trên từng sản phẩm") {
        @Override
        public void validateSetup(Double minOrderValue, Integer targetProductId) {
            if (targetProductId == null) {
                throw new IllegalArgumentException("Khuyến mãi cấp sản phẩm bắt buộc phải có ID sản phẩm mục tiêu");
            }
        }
    },
    ORDER("Khuyến mãi trên tổng đơn hàng") {
        @Override
        public void validateSetup(Double minOrderValue, Integer targetProductId) {
            if (targetProductId != null) {
                throw new IllegalArgumentException("Khuyến mãi cấp đơn hàng không được gắn với một sản phẩm cụ thể");
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
            throw new IllegalArgumentException("Phạm vi khuyến mãi không được để trống");
        }
        try {
            return PromotionScope.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Phạm vi không hợp lệ: " + value);
        }
    }
}