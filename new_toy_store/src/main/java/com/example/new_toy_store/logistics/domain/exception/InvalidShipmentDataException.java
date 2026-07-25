package com.example.new_toy_store.logistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidShipmentDataException extends LogisticsDomainException {

    public InvalidShipmentDataException(String field, String reason) {
        super(HttpStatus.BAD_REQUEST, "INVALID_SHIPMENT_DATA", reason, Map.of("field", field, "reason", reason));
    }
}
