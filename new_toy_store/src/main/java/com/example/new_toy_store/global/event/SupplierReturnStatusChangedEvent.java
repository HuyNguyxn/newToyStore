package com.example.new_toy_store.global.event;

import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;

import java.time.Instant;

public record SupplierReturnStatusChangedEvent(
        Integer returnId,
        Integer supplierId,
        Integer importNoteId,
        SupplierReturnStatus previousStatus,
        SupplierReturnStatus currentStatus,
        String actionBy,
        Instant occurredAt
) {
    public static SupplierReturnStatusChangedEvent now(Integer returnId,
                                                       Integer supplierId,
                                                       Integer importNoteId,
                                                       SupplierReturnStatus previousStatus,
                                                       SupplierReturnStatus currentStatus,
                                                       String actionBy) {
        return new SupplierReturnStatusChangedEvent(
                returnId,
                supplierId,
                importNoteId,
                previousStatus,
                currentStatus,
                actionBy,
                Instant.now()
        );
    }
}
