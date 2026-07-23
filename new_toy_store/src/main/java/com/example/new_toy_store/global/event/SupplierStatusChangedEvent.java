package com.example.new_toy_store.global.event;

import com.example.new_toy_store.supplier.domain.SupplierStatus;

import java.time.Instant;
import java.util.Objects;

public record SupplierStatusChangedEvent(
        Integer supplierId,
        SupplierStatus previousStatus,
        SupplierStatus currentStatus,
        Instant occurredAt
) {

    public SupplierStatusChangedEvent {
        Objects.requireNonNull(supplierId, "supplierId must not be null");
        Objects.requireNonNull(previousStatus, "previousStatus must not be null");
        Objects.requireNonNull(currentStatus, "currentStatus must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        if (previousStatus == currentStatus) {
            throw new IllegalArgumentException("A status change event requires different statuses");
        }
    }

    public static SupplierStatusChangedEvent now(
            Integer supplierId,
            SupplierStatus previousStatus,
            SupplierStatus currentStatus
    ) {
        return new SupplierStatusChangedEvent(supplierId, previousStatus, currentStatus, Instant.now());
    }
}
