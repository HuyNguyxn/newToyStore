package com.example.new_toy_store.global.event;

import com.example.new_toy_store.order.domain.OrderStatus;

import java.time.Instant;

public record OrderStatusChangedEvent(
        Integer orderId,
        Integer userId,
        OrderStatus previousStatus,
        OrderStatus currentStatus,
        String note,
        Instant occurredAt
) {
    public static OrderStatusChangedEvent now(
            Integer orderId,
            Integer userId,
            OrderStatus previousStatus,
            OrderStatus currentStatus,
            String note
    ) {
        return new OrderStatusChangedEvent(orderId, userId, previousStatus, currentStatus, note, Instant.now());
    }
}
