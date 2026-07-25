package com.example.new_toy_store.logistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateShipmentException extends LogisticsDomainException {

    public DuplicateShipmentException(Integer orderId) {
        super(
                HttpStatus.CONFLICT,
                "DUPLICATE_SHIPMENT",
                "This order already has a shipment.",
                Map.of("orderId", orderId)
        );
    }
}
