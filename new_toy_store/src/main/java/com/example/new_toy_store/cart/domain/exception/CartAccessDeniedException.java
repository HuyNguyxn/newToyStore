package com.example.new_toy_store.cart.domain.exception;

public class CartAccessDeniedException extends CartDomainException {
    public CartAccessDeniedException(Integer userId, Integer cartId) {
        super("Từ chối truy cập: Người dùng không có quyền thao tác trên giỏ hàng này.", "ACCESS_DENIED");
        addContext("userId", userId);
        addContext("cartId", cartId);
    }
}