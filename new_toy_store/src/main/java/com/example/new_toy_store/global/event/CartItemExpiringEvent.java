package com.example.new_toy_store.global.event;

public class CartItemExpiringEvent {

    private Integer userId;
    private Integer productId;
    private Integer variantId;
    private int daysLeft;

    public CartItemExpiringEvent(Integer userId, Integer productId, Integer variantId, int daysLeft) {
        this.userId = userId;
        this.productId = productId;
        this.variantId = variantId;
        this.daysLeft = daysLeft;
    }

    public Integer getUserId() { return userId; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public int getDaysLeft() { return daysLeft; }
}