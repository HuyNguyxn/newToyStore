package com.example.new_toy_store.cart.domain.exception;

public class CartNotFoundException extends RuntimeException {

    private final Integer userId;

    public CartNotFoundException(Integer userId) {
        super("Không tìm thấy giỏ hàng của người dùng (User ID: " + userId + ").");
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }
}