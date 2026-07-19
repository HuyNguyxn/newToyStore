package com.example.new_toy_store.global.event;

import java.util.List;

public class OrderCancelledEvent {
    private Integer orderId;
    private Integer userId;
    private String reason;
    private List<OrderItemPayload> items;

    public OrderCancelledEvent(Integer orderId, Integer userId, String reason, List<OrderItemPayload> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.reason = reason;
        this.items = items;
    }

    public Integer getOrderId() { return orderId; }
    public Integer getUserId() { return userId; }
    public String getReason() { return reason; }
    public List<OrderItemPayload> getItems() { return items; }

    public static class OrderItemPayload {
        private Integer variantId;
        private int quantity;

        public OrderItemPayload(Integer variantId, int quantity) {
            this.variantId = variantId;
            this.quantity = quantity;
        }

        public Integer getVariantId() { return variantId; }
        public int getQuantity() { return quantity; }
    }
}