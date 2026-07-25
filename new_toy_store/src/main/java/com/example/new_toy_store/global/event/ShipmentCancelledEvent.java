package com.example.new_toy_store.global.event;

import java.time.Instant;

public record ShipmentCancelledEvent(
        Integer shipmentId,
        Integer orderId,
        Integer userId,
        String trackingCode,
        String reason,
        Instant occurredAt
) {
    public static ShipmentCancelledEvent now(Integer shipmentId, Integer orderId, Integer userId, String trackingCode, String reason) {
        return new ShipmentCancelledEvent(shipmentId, orderId, userId, trackingCode, reason, Instant.now());
    }
}
