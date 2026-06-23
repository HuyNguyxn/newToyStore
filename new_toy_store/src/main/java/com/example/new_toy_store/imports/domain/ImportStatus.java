package com.example.new_toy_store.imports.domain;

public enum ImportStatus {

    PENDING("Chờ kiểm đếm") {
        @Override
        public boolean canComplete() {
            return true;
        }

        @Override
        public boolean canCancel() {
            return true;
        }

        @Override
        public boolean canModifyItems() {
            return true;
        }
    },

    COMPLETED("Đã nhập kho") {
        @Override
        public boolean canComplete() {
            return false;
        }

        @Override
        public boolean canCancel() {
            return false;
        }

        @Override
        public boolean canModifyItems() {
            return false;
        }
    },

    CANCELLED("Đã hủy") {
        @Override
        public boolean canComplete() {
            return false;
        }

        @Override
        public boolean canCancel() {
            return false;
        }

        @Override
        public boolean canModifyItems() {
            return false;
        }
    };

    private final String displayName;

    ImportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean canComplete();
    public abstract boolean canCancel();
    public abstract boolean canModifyItems();

    public static ImportStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái phiếu nhập không được để trống");
        }
        try {
            return ImportStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái phiếu nhập không hợp lệ: " + value);
        }
    }
}