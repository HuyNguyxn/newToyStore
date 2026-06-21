package com.example.new_toy_store.cart.application.dto.response;

import java.util.List;

public class CartResponse {

    private Integer id;
    private Integer userId;
    private double cartTotal;
    private List<CartItemResponse> items;

    public CartResponse(Integer id, Integer userId, double cartTotal, List<CartItemResponse> items) {
        this.id = id;
        this.userId = userId;
        this.cartTotal = cartTotal;
        this.items = items;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public double getCartTotal() { return cartTotal; }
    public List<CartItemResponse> getItems() { return items; }
}