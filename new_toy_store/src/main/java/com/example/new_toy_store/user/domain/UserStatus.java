package com.example.new_toy_store.user.domain;

public enum UserStatus {

    UNVERIFIED("Chưa xác thực email") {
        @Override
        public boolean canLogin() {
            return false;
        }

        @Override
        public boolean canPlaceOrder() {
            return false;
        }

        @Override
        public boolean canModifyData() {
            return true;
        }
    },

    ACTIVE("Đang hoạt động") {
        @Override
        public boolean canLogin() {
            return true;
        }

        @Override
        public boolean canPlaceOrder() {
            return true;
        }

        @Override
        public boolean canModifyData() {
            return true;
        }
    },

    LOCKED("Tài khoản bị khóa") {
        @Override
        public boolean canLogin() {
            return false;
        }

        @Override
        public boolean canPlaceOrder() {
            return false;
        }

        @Override
        public boolean canModifyData() {
            return false;
        }
    };

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean canLogin();
    public abstract boolean canPlaceOrder();
    public abstract boolean canModifyData();

    public static UserStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái người dùng không được để trống");
        }
        try {
            return UserStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái người dùng không hợp lệ: " + value);
        }
    }
}