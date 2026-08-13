package com.example.new_toy_store.cart.domain.exception;

public class CartAccessDeniedException extends CartDomainException {

    private CartAccessDeniedException(String message, Integer userId, Integer cartId, String action) {
        super(message, "CART_ACCESS_DENIED");
        addContext("userId", userId);
        addContext("cartId", cartId);
        addContext("action", action);
    }

    public static CartAccessDeniedException forCart(Integer userId, Integer cartId, String action) {
        return new CartAccessDeniedException(
                "Từ chối truy cập: Người dùng không có quyền " + action + " trên giỏ hàng này.",
                userId,
                cartId,
                action
        );
    }

    public static CartAccessDeniedException forUserCart(Integer currentUserId, Integer targetUserId, String action) {
        CartAccessDeniedException exception = new CartAccessDeniedException(
                "Bạn không có quyền " + action + " giỏ hàng của người dùng khác.",
                currentUserId,
                null,
                action
        );
        exception.addContext("targetUserId", targetUserId);
        return exception;
    }
}
