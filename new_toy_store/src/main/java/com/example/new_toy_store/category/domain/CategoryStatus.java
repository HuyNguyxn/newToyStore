package com.example.new_toy_store.category.domain;

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
}