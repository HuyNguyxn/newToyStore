package com.example.new_toy_store.order.domain;

public enum OrderStatus {

    PENDING("Chờ xác nhận") {
        @Override
        public void confirm(Order order, String note) {
            order.changeStatus(CONFIRMED, note);
        }

        @Override
        public void cancel(Order order, String note) {
            order.changeStatus(CANCELLED, note);
        }

        @Override
        public boolean canBeDeleted() {
            return true;
        }
    },

    CONFIRMED("Đã xác nhận") {
        @Override
        public void ship(Order order, String note) {
            order.changeStatus(SHIPPED, note);
        }

        @Override
        public void cancel(Order order, String note) {
            order.changeStatus(CANCELLED, note);
        }
    },

    SHIPPED("Đang giao") {
        @Override
        public void complete(Order order, String note) {
            order.changeStatus(COMPLETED, note);
        }
    },

    COMPLETED("Hoàn thành"),

    CANCELLED("Đã hủy") {
        @Override
        public boolean canBeDeleted() {
            return true;
        }
    };

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void confirm(Order order, String note) {
        throw notAllowed("Xác nhận");
    }

    public void ship(Order order, String note) {
        throw notAllowed("Giao hàng");
    }

    public void complete(Order order, String note) {
        throw notAllowed("Hoàn thành");
    }

    public void cancel(Order order, String note) {
        throw notAllowed("Hủy");
    }

    public boolean canBeDeleted() {
        return false;
    }

    private RuntimeException notAllowed(String action) {
        return new IllegalStateException("Không thể thực hiện thao tác '" + action + "' khi đơn hàng đang ở trạng thái: " + this.displayName);
    }

    public static OrderStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không được để trống");
        }
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: " + value);
        }
    }
}