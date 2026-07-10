package com.example.new_toy_store.order.domain.exception;

public class OrderNotFoundException extends RuntimeException {
    private final Integer orderId;

    public OrderNotFoundException(Integer orderId) {
        super("Không tìm thấy đơn hàng với ID: " + orderId);
        this.orderId = orderId;
    }

    public Integer getOrderId() { return orderId; }
}