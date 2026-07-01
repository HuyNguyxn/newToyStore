package com.example.new_toy_store.cart.domain.exception;

public class CartItemNotFoundException extends RuntimeException {

    private final Integer itemId;

    public CartItemNotFoundException(Integer itemId) {
        super("Sản phẩm (Item ID: " + itemId + ") không tồn tại trong giỏ hàng.");
        this.itemId = itemId;
    }

    public Integer getItemId() {
        return itemId;
    }
}