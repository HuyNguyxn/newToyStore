package com.example.new_toy_store.order.application.dto.response;

import com.example.new_toy_store.order.domain.OrderStatus;

public class OrderPaymentSnapshot {

    private final Integer orderId;
    private final Integer userId;
    private final OrderStatus status;
    private final double payableAmount;

    public OrderPaymentSnapshot(Integer orderId, Integer userId, OrderStatus status, double payableAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.payableAmount = Math.max(0.0, Math.round(payableAmount * 100.0) / 100.0);
    }

    public Integer getOrderId() { return orderId; }
    public Integer getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public double getPayableAmount() { return payableAmount; }
}
