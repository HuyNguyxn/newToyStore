package com.example.new_toy_store.order.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class OrderAccessDeniedException extends OrderDomainException {

    private final Integer orderId;
    private final Integer currentUserId;
    private final String action;

    public OrderAccessDeniedException(Integer orderId, Integer currentUserId, String action) {
        super(
                HttpStatus.FORBIDDEN,
                "ORDER_ACCESS_DENIED",
                "Người dùng ID " + currentUserId + " không có quyền " + action + " đơn hàng ID " + orderId + ".",
                Map.of(
                        "orderId", orderId,
                        "currentUserId", currentUserId,
                        "action", action
                )
        );
        this.orderId = orderId;
        this.currentUserId = currentUserId;
        this.action = action;
    }

    public Integer getOrderId() { return orderId; }
    public Integer getCurrentUserId() { return currentUserId; }
    public String getAction() { return action; }
}
