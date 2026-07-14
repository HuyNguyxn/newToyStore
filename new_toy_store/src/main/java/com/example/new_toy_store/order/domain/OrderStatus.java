package com.example.new_toy_store.order.domain;

import com.example.new_toy_store.order.domain.exception.InvalidOrderOperationException;
import com.example.new_toy_store.order.domain.exception.InvalidOrderStatusException;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum OrderStatus {

    PENDING("Chờ xác nhận") {
        @Override public void confirm(Order order, String note) { order.changeStatus(CONFIRMED, note); }
        @Override public void cancel(Order order, String note) { order.changeStatus(CANCELLED, note); }
        @Override public boolean canBeDeleted() { return true; }
        @Override public boolean canModifyShippingInfo() { return true; }
        @Override public List<OrderStatus> getNextValidStates() { return Arrays.asList(CONFIRMED, CANCELLED); }
    },

    CONFIRMED("Đã xác nhận") {
        @Override public void ship(Order order, String note) { order.changeStatus(SHIPPED, note); }
        @Override public void cancel(Order order, String note) { order.changeStatus(CANCELLED, note); }
        @Override public boolean canModifyShippingInfo() { return true; }
        @Override public List<OrderStatus> getNextValidStates() { return Arrays.asList(SHIPPED, CANCELLED); }
    },

    SHIPPED("Đang giao") {
        @Override public void complete(Order order, String note) { order.changeStatus(COMPLETED, note); }
        @Override public List<OrderStatus> getNextValidStates() { return Collections.singletonList(COMPLETED); }
    },

    COMPLETED("Hoàn thành") {
        @Override public void refundPartially(Order order, String note) { order.changeStatus(PARTIALLY_REFUNDED, note); }
        @Override public void refundFully(Order order, String note) { order.changeStatus(FULLY_REFUNDED, note); }
        @Override public List<OrderStatus> getNextValidStates() { return Arrays.asList(PARTIALLY_REFUNDED, FULLY_REFUNDED); }
    },

    PARTIALLY_REFUNDED("Đã hoàn trả một phần") {
        @Override public void refundPartially(Order order, String note) { order.changeStatus(PARTIALLY_REFUNDED, note); }
        @Override public void refundFully(Order order, String note) { order.changeStatus(FULLY_REFUNDED, note); }
        @Override public List<OrderStatus> getNextValidStates() { return Arrays.asList(PARTIALLY_REFUNDED, FULLY_REFUNDED); }
    },

    FULLY_REFUNDED("Đã hoàn trả toàn bộ") {
        @Override public List<OrderStatus> getNextValidStates() { return Collections.emptyList(); }
    },

    CANCELLED("Đã hủy") {
        @Override public boolean canBeDeleted() { return true; }
        @Override public List<OrderStatus> getNextValidStates() { return Collections.emptyList(); }
    };

    private final String displayName;

    OrderStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public void confirm(Order order, String note) { throw notAllowed("Xác nhận"); }
    public void ship(Order order, String note) { throw notAllowed("Giao hàng"); }
    public void complete(Order order, String note) { throw notAllowed("Hoàn thành"); }
    public void cancel(Order order, String note) { throw notAllowed("Hủy"); }

    public void refundPartially(Order order, String note) { throw notAllowed("Hoàn trả một phần"); }
    public void refundFully(Order order, String note) { throw notAllowed("Hoàn trả toàn bộ"); }

    public boolean canBeDeleted() { return false; }
    public boolean canModifyShippingInfo() { return false; }

    public abstract List<OrderStatus> getNextValidStates();

    private RuntimeException notAllowed(String action) {
        return new InvalidOrderOperationException(this.displayName, action);
    }

    @JsonCreator
    public static OrderStatus from(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            List<String> allowed = Arrays.stream(OrderStatus.values()).map(Enum::name).collect(Collectors.toList());
            throw new InvalidOrderStatusException(value, allowed);
        }
    }
}