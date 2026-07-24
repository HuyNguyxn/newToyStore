package com.example.new_toy_store.order.domain;

import com.example.new_toy_store.order.domain.exception.InvalidOrderStatusException;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum OrderStatus {

    PENDING("Chờ xác nhận") {
        @Override public boolean canBeDeleted() { return true; }
        @Override public boolean canModifyShippingInfo() { return true; }
        @Override public List<OrderStatus> getNextValidStates() { return List.of(CONFIRMED, CANCELLED); }
    },

    CONFIRMED("Đã xác nhận") {
        @Override public boolean canModifyShippingInfo() { return true; }
        @Override public List<OrderStatus> getNextValidStates() { return List.of(SHIPPED, CANCELLED); }
    },

    SHIPPED("Đang giao") {
        @Override public List<OrderStatus> getNextValidStates() { return List.of(COMPLETED); }
    },

    COMPLETED("Hoàn thành") {
        @Override public List<OrderStatus> getNextValidStates() { return List.of(PARTIALLY_REFUNDED, FULLY_REFUNDED); }
    },

    PARTIALLY_REFUNDED("Đã hoàn trả một phần") {
        @Override public List<OrderStatus> getNextValidStates() { return List.of(PARTIALLY_REFUNDED, FULLY_REFUNDED); }
    },

    FULLY_REFUNDED("Đã hoàn trả toàn bộ") {
        @Override public List<OrderStatus> getNextValidStates() { return List.of(); }
    },

    CANCELLED("Đã hủy") {
        @Override public boolean canBeDeleted() { return true; }
        @Override public List<OrderStatus> getNextValidStates() { return List.of(); }
    };

    private final String displayName;

    OrderStatus(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }
    public boolean canBeDeleted() { return false; }
    public boolean canModifyShippingInfo() { return false; }
    public abstract List<OrderStatus> getNextValidStates();

    public boolean canTransitionTo(OrderStatus targetStatus) {
        return targetStatus != null && getNextValidStates().contains(targetStatus);
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
