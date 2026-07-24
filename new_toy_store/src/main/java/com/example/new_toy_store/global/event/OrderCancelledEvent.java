package com.example.new_toy_store.global.event;

import java.util.List;

public class OrderCancelledEvent {
    private Integer orderId;
    private Integer userId;
    private String reason;
    private List<OrderCancelledItemPayload> items;

    public OrderCancelledEvent(Integer orderId, Integer userId, String reason, List<OrderCancelledItemPayload> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.reason = reason;
        this.items = items;
    }

    public Integer getOrderId() { return orderId; }
    public Integer getUserId() { return userId; }
    public String getReason() { return reason; }
    public List<OrderCancelledItemPayload> getItems() { return items; }
}
