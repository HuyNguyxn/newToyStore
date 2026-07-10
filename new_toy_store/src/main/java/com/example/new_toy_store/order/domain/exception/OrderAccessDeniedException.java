package com.example.new_toy_store.order.domain.exception;

public class OrderAccessDeniedException extends RuntimeException {
    private final Integer orderId;
    private final Integer currentUserId;
    private final String action;

    public OrderAccessDeniedException(Integer orderId, Integer currentUserId, String action) {
        super(String.format("Người dùng ID [%d] không có quyền %s đơn hàng ID [%d].", currentUserId, action, orderId));
        this.orderId = orderId;
        this.currentUserId = currentUserId;
        this.action = action;
    }

    public Integer getOrderId() { return orderId; }
    public Integer getCurrentUserId() { return currentUserId; }
    public String getAction() { return action; }
}