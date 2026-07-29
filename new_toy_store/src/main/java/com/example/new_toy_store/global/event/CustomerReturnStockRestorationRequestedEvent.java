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
        Instant occurredAt
) {

    public static CustomerReturnStockRestorationRequestedEvent now(
            Integer returnId,
            Integer orderId,
            Map<Integer, Integer> variantQuantities
    ) {
        return new CustomerReturnStockRestorationRequestedEvent(
                returnId,
                orderId,
                Map.copyOf(variantQuantities),
                Instant.now()
        );
    }
}
