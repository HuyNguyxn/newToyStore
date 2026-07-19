package com.example.new_toy_store.cart.domain.exception;

public class CartItemNotFoundException extends CartDomainException {
    public CartItemNotFoundException(Integer itemId) {
        super("Sản phẩm (Item ID: " + itemId + ") không tồn tại trong giỏ hàng.", "ITEM_NOT_FOUND");
        addContext("itemId", itemId);
    }
}