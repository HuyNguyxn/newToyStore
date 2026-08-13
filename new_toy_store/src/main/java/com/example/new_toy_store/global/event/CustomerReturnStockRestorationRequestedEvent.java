package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.Map;

/**
 * Published after a return passes quality control and sellable variants can be returned to stock.
 */
public record CustomerReturnStockRestorationRequestedEvent(
        Integer returnId,
        Integer orderId,
        Map<Integer, Integer> variantQuantities,
        double restoredCostAmount,
        Instant occurredAt
) {

    public static CustomerReturnStockRestorationRequestedEvent now(
            Integer returnId,
            Integer orderId,
            Map<Integer, Integer> variantQuantities,
            double restoredCostAmount
    ) {
        return new CustomerReturnStockRestorationRequestedEvent(
                returnId,
                orderId,
                Map.copyOf(variantQuantities),
                Math.max(0.0, Math.round(restoredCostAmount * 100.0) / 100.0),
                Instant.now()
        );
    }
}
