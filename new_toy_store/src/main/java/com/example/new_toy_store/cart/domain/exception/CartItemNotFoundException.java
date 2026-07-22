package com.example.new_toy_store.cart.domain.exception;

public class CartItemNotFoundException extends CartDomainException {

    public CartItemNotFoundException(Integer itemId) {
        super("Không tìm thấy mục giỏ hàng (Item ID: " + itemId + ").", "CART_ITEM_NOT_FOUND");
        addContext("itemId", itemId);
    }

    public static CartItemNotFoundException byItemId(Integer itemId) {
        return new CartItemNotFoundException(itemId);
    }
}
