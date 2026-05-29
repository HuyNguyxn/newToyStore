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
        throw notAllowed("confirm");
    }

    public void ship(Order order, String note) {
        throw notAllowed("ship");
    }

    public void complete(Order order, String note) {
        throw notAllowed("complete");
    }

    public void cancel(Order order, String note) {
        throw notAllowed("cancel");
    }

    public boolean canBeDeleted() {
        return false;
    }

    private RuntimeException notAllowed(String action) {
        return new RuntimeException("Cannot " + action + " from " + this.name());
    }

    public static OrderStatus from(String value) {
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status: " + value);
        }
    }
}