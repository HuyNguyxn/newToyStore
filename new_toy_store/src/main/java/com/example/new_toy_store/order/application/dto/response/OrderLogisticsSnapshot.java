package com.example.new_toy_store.order.application.dto.response;

import com.example.new_toy_store.order.domain.OrderStatus;

import java.util.List;

public class OrderLogisticsSnapshot {

    private final Integer orderId;
    private final Integer userId;
    private final OrderStatus status;
    private final double totalAmount;
    private final String shippingAddress;
    private final List<OrderLogisticsItemSnapshot> items;

    public OrderLogisticsSnapshot(
            Integer orderId,
            Integer userId,
            OrderStatus status,
            double totalAmount,
            String shippingAddress,
            List<OrderLogisticsItemSnapshot> items
    ) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.totalAmount = Math.max(0.0, Math.round(totalAmount * 100.0) / 100.0);
        this.shippingAddress = shippingAddress;
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    public Integer getOrderId() { return orderId; }
    public Integer getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public List<OrderLogisticsItemSnapshot> getItems() { return items; }
}
