package com.example.new_toy_store.global.event;

public class OrderCreationFailedEvent {
    private Integer cartId;
    private Integer userId;
    private String reason;

    public OrderCreationFailedEvent(Integer cartId, Integer userId, String reason) {
        this.cartId = cartId;
        this.userId = userId;
        this.reason = reason;
    }

    public Integer getCartId() { return cartId; }
    public Integer getUserId() { return userId; }
    public String getReason() { return reason; }
}