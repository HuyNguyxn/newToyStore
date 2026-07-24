package com.example.new_toy_store.order.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class OrderNotFoundException extends OrderDomainException {

    private final Integer orderId;

    public OrderNotFoundException(Integer orderId) {
        super(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                "Không tìm thấy đơn hàng có ID " + orderId + ".",
                Map.of(
                        "orderId", orderId,
                        "entity", "Order"
                )
        );
        this.orderId = orderId;
    }

    public Integer getOrderId() { return orderId; }
}
