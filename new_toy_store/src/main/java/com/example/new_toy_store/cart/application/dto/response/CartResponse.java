package com.example.new_toy_store.cart.application.dto.response;

import java.util.List;

public class CartResponse {
    private Integer cartId;
    private Integer userId;
    private double cartTotal;
    private String appliedPromoCode;
    private double orderDiscountAmount;
    private double finalTotal;
    private String promoMessage;
    private List<CartItemResponse> items;
    private List<String> allowedActions;

    public CartResponse(Integer cartId, Integer userId, double cartTotal, String appliedPromoCode,
                        double orderDiscountAmount, double finalTotal, String promoMessage,
                        List<CartItemResponse> items, List<String> allowedActions) {
        this.cartId = cartId;
        this.userId = userId;
        this.cartTotal = cartTotal;
        this.appliedPromoCode = appliedPromoCode;
        this.orderDiscountAmount = orderDiscountAmount;
        this.finalTotal = finalTotal;
        this.promoMessage = promoMessage;
        this.items = items;
        this.allowedActions = allowedActions;
    }

    public Integer getCartId() { return cartId; }
    public Integer getUserId() { return userId; }
    public double getCartTotal() { return cartTotal; }
    public String getAppliedPromoCode() { return appliedPromoCode; }
    public double getOrderDiscountAmount() { return orderDiscountAmount; }
    public double getFinalTotal() { return finalTotal; }
    public String getPromoMessage() { return promoMessage; }
    public List<CartItemResponse> getItems() { return items; }
    public List<String> getAllowedActions() { return allowedActions; }
}