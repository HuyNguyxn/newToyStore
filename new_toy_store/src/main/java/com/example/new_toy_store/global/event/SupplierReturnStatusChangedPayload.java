package com.example.new_toy_store.global.event;

import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;

public record SupplierReturnStatusChangedPayload(
        Integer returnId,
        Integer supplierId,
        Integer importNoteId,
        SupplierReturnStatus previousStatus,
        SupplierReturnStatus currentStatus,
        String actionBy
) {
    public static SupplierReturnStatusChangedPayload of(Integer returnId,
                                                        Integer supplierId,
                                                        Integer importNoteId,
                                                        SupplierReturnStatus previousStatus,
                                                        SupplierReturnStatus currentStatus,
                                                        String actionBy) {
        return new SupplierReturnStatusChangedPayload(
                returnId,
                supplierId,
                importNoteId,
                previousStatus,
                currentStatus,
                actionBy
        );
    }
}
