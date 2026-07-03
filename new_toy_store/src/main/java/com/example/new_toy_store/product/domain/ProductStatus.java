package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;

public enum ProductStatus {

    ACTIVE("Đang kinh doanh") {
        @Override
        public boolean canBePurchased() {
            return true;
        }

        @Override
        public boolean isVisible() {
            return true;
        }
    },

    INACTIVE("Ngừng kinh doanh") {
        @Override
        public boolean canBePurchased() {
            return false;
        }

        @Override
        public boolean isVisible() {
            return false;
        }
    },

    OUT_OF_STOCK("Hết hàng") {
        @Override
        public boolean canBePurchased() {
            return false;
        }

        @Override
        public boolean isVisible() {
            return true;
        }
    };

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean canBePurchased();
    public abstract boolean isVisible();

    public static ProductStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidProductOperationException.emptyStatus();
        }
        try {
            return ProductStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidProductOperationException.invalidStatus(value);
        }
    }
}