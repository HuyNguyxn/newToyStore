package com.example.new_toy_store.logistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ShipmentNotFoundException extends LogisticsDomainException {

    public ShipmentNotFoundException(Integer shipmentId) {
        super(HttpStatus.NOT_FOUND, "SHIPMENT_NOT_FOUND", "Shipment was not found.", Map.of("shipmentId", shipmentId));
    }
}
