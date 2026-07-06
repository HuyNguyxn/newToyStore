package com.example.new_toy_store.category.domain;

import com.example.new_toy_store.category.domain.exception.InvalidCategoryOperationException;

public enum CategoryStatus {

    VISIBLE("Đang hiển thị") {
        @Override
        public boolean isVisibleToCustomers() {
            return true;
        }
    },

    HIDDEN("Đang ẩn") {
        @Override
        public boolean isVisibleToCustomers() {
            return false;
        }
    };

    private final String displayName;

    CategoryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean isVisibleToCustomers();

    public static CategoryStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidCategoryOperationException.emptyStatus();
        }
        try {
            return CategoryStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidCategoryOperationException.invalidStatus(value);
        }
    }
}