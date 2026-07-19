package com.example.new_toy_store.global.event;

import java.util.List;

public class OrderCreatedEvent {
    private Integer orderId;
    private Integer cartId;
    private Integer userId;
    private List<OrderItemPayload> items;

    public OrderCreatedEvent(Integer orderId, Integer cartId, Integer userId, List<OrderItemPayload> items) {
        this.orderId = orderId;
        this.cartId = cartId;
        this.userId = userId;
        this.items = items;
    }

    public Integer getOrderId() { return orderId; }
    public Integer getCartId() { return cartId; }
    public Integer getUserId() { return userId; }
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