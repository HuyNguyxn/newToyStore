package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.List;

public record SupplierReturnStockRestorationRequestedEvent(
        Integer returnId,
        Integer supplierId,
        List<ReturnItemDetail> items,
        double inventoryAmount,
        double refundAmount,
        Instant occurredAt
) {
    public static SupplierReturnStockRestorationRequestedEvent now(
            Integer returnId,
            Integer supplierId,
            List<ReturnItemDetail> items,
            double inventoryAmount,
            double refundAmount
    ) {
        return new SupplierReturnStockRestorationRequestedEvent(
                returnId, supplierId, List.copyOf(items), inventoryAmount, refundAmount, Instant.now()
        );
    }

    public record ReturnItemDetail(Integer variantId, String batchNumber, int quantity) {}
}
