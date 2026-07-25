package com.example.new_toy_store.global.event;

import java.time.Instant;

public record ShipmentInTransitEvent(
        Integer shipmentId,
        Integer orderId,
        Integer userId,
        String trackingCode,
        Instant occurredAt
) {
    public static ShipmentInTransitEvent now(Integer shipmentId, Integer orderId, Integer userId, String trackingCode) {
        return new ShipmentInTransitEvent(shipmentId, orderId, userId, trackingCode, Instant.now());
    }
}
