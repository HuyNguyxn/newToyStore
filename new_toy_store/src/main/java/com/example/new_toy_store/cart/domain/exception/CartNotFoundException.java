package com.example.new_toy_store.cart.domain.exception;

public class CartNotFoundException extends CartDomainException {

    private CartNotFoundException(String message, String idType, Integer id) {
        super(message, "CART_NOT_FOUND");
        addContext(idType, id);
    }

    public static CartNotFoundException byUserId(Integer userId) {
        return new CartNotFoundException(
                "Không tìm thấy giỏ hàng của người dùng (User ID: " + userId + ").",
                "userId",
                userId
        );
    }

    public static CartNotFoundException byCartId(Integer cartId) {
        return new CartNotFoundException(
                "Không tìm thấy giỏ hàng (Cart ID: " + cartId + ").",
                "cartId",
                cartId
        );
    }
}
