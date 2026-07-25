package com.example.new_toy_store.global.event;

import java.time.Instant;

public record ShipmentDeliveredEvent(
        Integer shipmentId,
        Integer orderId,
        Integer userId,
        String trackingCode,
        double codAmount,
        Instant occurredAt
) {
    public static ShipmentDeliveredEvent now(Integer shipmentId, Integer orderId, Integer userId, String trackingCode, double codAmount) {
        return new ShipmentDeliveredEvent(shipmentId, orderId, userId, trackingCode, codAmount, Instant.now());
    }
}
