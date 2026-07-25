package com.example.new_toy_store.global.event;

import java.time.Instant;

public record ShipmentReturnedEvent(
        Integer shipmentId,
        Integer orderId,
        Integer userId,
        String trackingCode,
        String reason,
        Instant occurredAt
) {
    public static ShipmentReturnedEvent now(Integer shipmentId, Integer orderId, Integer userId, String trackingCode, String reason) {
        return new ShipmentReturnedEvent(shipmentId, orderId, userId, trackingCode, reason, Instant.now());
    }
}
