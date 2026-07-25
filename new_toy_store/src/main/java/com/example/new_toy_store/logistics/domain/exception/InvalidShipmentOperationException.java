package com.example.new_toy_store.logistics.domain.exception;

import com.example.new_toy_store.logistics.domain.ShipmentStatus;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidShipmentOperationException extends LogisticsDomainException {

    public InvalidShipmentOperationException(String action, String reason) {
        super(HttpStatus.BAD_REQUEST, "INVALID_SHIPMENT_OPERATION", reason, Map.of("action", action, "reason", reason));
    }

    public static InvalidShipmentOperationException invalidTransition(
            Integer shipmentId,
            ShipmentStatus currentStatus,
            ShipmentStatus nextStatus
    ) {
        return new InvalidShipmentOperationException(
                "changeStatus",
                "Shipment status cannot change from " + currentStatus.name() + " to " + nextStatus.name() + ".",
                Map.of("shipmentId", shipmentId, "currentStatus", currentStatus.name(), "nextStatus", nextStatus.name())
        );
    }

    private InvalidShipmentOperationException(String action, String reason, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, "INVALID_SHIPMENT_OPERATION", reason, contextData);
    }
}
