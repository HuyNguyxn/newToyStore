package com.example.new_toy_store.order.domain;

import com.example.new_toy_store.order.domain.exception.InvalidOrderStatusException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrderStatus {

    PENDING("Chờ xác nhận", "Đơn hàng mới được tạo và đang chờ xác nhận") {
        @Override public boolean canBeDeleted() { return true; }
        @Override public boolean canModifyShippingInfo() { return true; }
        @Override public List<OrderStatus> getNextValidStates() { return List.of(CONFIRMED, CANCELLED); }
    },

    CONFIRMED("Đã xác nhận", "Đơn hàng đã được xác nhận và có thể bắt đầu giao") {
        @Override public boolean canModifyShippingInfo() { return true; }
        @Override public List<OrderStatus> getNextValidStates() { return List.of(SHIPPED, CANCELLED); }
    },

    SHIPPED("Đang giao", "Đơn hàng đang được giao đến khách hàng") {
        @Override public List<OrderStatus> getNextValidStates() { return List.of(COMPLETED); }
    },

    COMPLETED("Hoàn thành", "Đơn hàng đã giao thành công") {
        @Override public List<OrderStatus> getNextValidStates() { return List.of(PARTIALLY_REFUNDED, FULLY_REFUNDED); }
    },

    PARTIALLY_REFUNDED("Đã hoàn trả một phần", "Một phần sản phẩm trong đơn hàng đã được hoàn trả") {
        @Override public List<OrderStatus> getNextValidStates() { return List.of(PARTIALLY_REFUNDED, FULLY_REFUNDED); }
    },

    FULLY_REFUNDED("Đã hoàn trả toàn bộ", "Toàn bộ đơn hàng đã được hoàn trả") {
        @Override public List<OrderStatus> getNextValidStates() { return List.of(); }
    },

    CANCELLED("Đã hủy", "Đơn hàng đã bị hủy") {
        @Override public boolean canBeDeleted() { return true; }
        @Override public List<OrderStatus> getNextValidStates() { return List.of(); }
    };

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean canBeDeleted() { return false; }
    public boolean canModifyShippingInfo() { return false; }

    @JsonIgnore
    public abstract List<OrderStatus> getNextValidStates();

    public boolean canTransitionTo(OrderStatus targetStatus) {
        return targetStatus != null && getNextValidStates().contains(targetStatus);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
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
