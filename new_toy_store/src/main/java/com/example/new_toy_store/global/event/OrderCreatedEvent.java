package com.example.new_toy_store.global.event;

import java.util.List;

public class OrderCreatedEvent {
    private Integer orderId;
    private Integer cartId;
    private Integer userId;
    private List<OrderCreatedItemPayload> items;

    public OrderCreatedEvent(Integer orderId, Integer cartId, Integer userId, List<OrderCreatedItemPayload> items) {
        this.orderId = orderId;
        this.cartId = cartId;
        this.userId = userId;
        this.items = items;
    }

    public Integer getOrderId() { return orderId; }
    public Integer getCartId() { return cartId; }
    public Integer getUserId() { return userId; }
    public List<OrderCreatedItemPayload> getItems() { return items; }
}
