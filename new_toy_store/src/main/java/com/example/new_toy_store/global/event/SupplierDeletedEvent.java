package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.Objects;

public record SupplierDeletedEvent(
        Integer supplierId,
        String phoneNumber,
        Instant occurredAt
) {

    public SupplierDeletedEvent {
        Objects.requireNonNull(supplierId, "supplierId must not be null");
        Objects.requireNonNull(phoneNumber, "phoneNumber must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public static SupplierDeletedEvent now(Integer supplierId, String phoneNumber) {
        return new SupplierDeletedEvent(supplierId, phoneNumber, Instant.now());
    }
}
