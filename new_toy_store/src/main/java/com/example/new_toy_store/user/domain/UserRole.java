package com.example.new_toy_store.user.domain;

public enum UserRole {

    CUSTOMER("Khách hàng") {
        @Override
        public boolean canManageProducts() {
            return false;
        }

        @Override
        public boolean canManageOrders() {
            return false;
        }
    },

    ADMIN("Quản trị viên") {
        @Override
        public boolean canManageProducts() {
            return true;
        }

        @Override
        public boolean canManageOrders() {
            return true;
        }
    };

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean canManageProducts();
    public abstract boolean canManageOrders();

    public static UserRole from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Vai trò người dùng không được để trống.");
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Vai trò người dùng không hợp lệ: " + value);
        }
    }
}