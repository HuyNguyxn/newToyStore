package com.example.new_toy_store.logistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ShipmentAccessDeniedException extends LogisticsDomainException {

    public ShipmentAccessDeniedException(Integer shipmentId, Integer userId, String action) {
        super(
                HttpStatus.FORBIDDEN,
                "SHIPMENT_ACCESS_DENIED",
                "You do not have permission to " + action + " this shipment.",
                Map.of("shipmentId", shipmentId, "userId", userId, "action", action)
        );
    }
}
