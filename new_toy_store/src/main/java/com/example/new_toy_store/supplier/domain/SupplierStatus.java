package com.example.new_toy_store.supplier.domain;

public enum SupplierStatus {

    ACTIVE("Đang hợp tác") {
        @Override
        public boolean canImport() {
            return true;
        }
    },

    SUSPENDED("Tạm ngưng") {
        @Override
        public boolean canImport() {
            return false;
        }
    },

    BLACKLISTED("Cấm hợp tác") {
        @Override
        public boolean canImport() {
            return false;
        }
    };

    private final String displayName;

    SupplierStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean canImport();
}