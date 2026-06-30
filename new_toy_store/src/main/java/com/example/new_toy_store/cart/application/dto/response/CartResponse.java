package com.example.new_toy_store.cart.application.dto.response;

import java.util.List;

public class CartResponse {

    private Integer id;
    private Integer userId;
    private double cartTotal;

    private String appliedPromoCode;
    private double orderDiscountAmount;
    private double finalTotal;
    private String promoMessage;

    private List<CartItemResponse> items;

    public CartResponse(Integer id, Integer userId, double cartTotal, String appliedPromoCode,
                        double orderDiscountAmount, double finalTotal, String promoMessage,
                        List<CartItemResponse> items) {
        this.id = id;
        this.userId = userId;
        this.cartTotal = cartTotal;
        this.appliedPromoCode = appliedPromoCode;
        this.orderDiscountAmount = orderDiscountAmount;
        this.finalTotal = finalTotal;
        this.promoMessage = promoMessage;
        this.items = items;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public double getCartTotal() { return cartTotal; }
    public String getAppliedPromoCode() { return appliedPromoCode; }
    public double getOrderDiscountAmount() { return orderDiscountAmount; }
    public double getFinalTotal() { return finalTotal; }
    public String getPromoMessage() { return promoMessage; }
    public List<CartItemResponse> getItems() { return items; }
}