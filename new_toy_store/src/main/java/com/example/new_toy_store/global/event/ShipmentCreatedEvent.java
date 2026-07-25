package com.example.new_toy_store.global.event;

import java.time.Instant;

public record ShipmentCreatedEvent(
        Integer shipmentId,
        Integer orderId,
        Integer userId,
        String trackingCode,
        double codAmount,
        Instant occurredAt
) {
    public static ShipmentCreatedEvent now(Integer shipmentId, Integer orderId, Integer userId, String trackingCode, double codAmount) {
        return new ShipmentCreatedEvent(shipmentId, orderId, userId, trackingCode, codAmount, Instant.now());
    }
}
