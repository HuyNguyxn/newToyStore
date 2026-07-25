package com.example.new_toy_store.logistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class InvalidShipmentStatusException extends LogisticsDomainException {

    private InvalidShipmentStatusException(String message, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, "INVALID_SHIPMENT_STATUS", message, contextData);
    }

    public static InvalidShipmentStatusException emptyStatus() {
        return new InvalidShipmentStatusException("Shipment status must not be empty.", Map.of("field", "status"));
    }

    public static InvalidShipmentStatusException invalidStatus(String value, List<String> allowedStatuses) {
        return new InvalidShipmentStatusException(
                "Shipment status [" + value + "] is invalid.",
                Map.of("value", value, "allowedStatuses", allowedStatuses)
        );
    }
}
