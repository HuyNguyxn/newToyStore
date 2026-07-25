package com.example.new_toy_store.logistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ShipmentDeletedConflictException extends LogisticsDomainException {

    public ShipmentDeletedConflictException(Integer shipmentId) {
        super(
                HttpStatus.CONFLICT,
                "SHIPMENT_DELETED_CONFLICT",
                "Shipment was already deleted or changed by another request.",
                Map.of("shipmentId", shipmentId)
        );
    }
}
