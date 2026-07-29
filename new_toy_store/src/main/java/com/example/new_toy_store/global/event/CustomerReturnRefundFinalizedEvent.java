package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.Map;

/**
 * Published after a return is finalized as refunded so the order module can update its refund state.
 */
public record CustomerReturnRefundFinalizedEvent(
        Integer returnId,
        Integer orderId,
        Map<Integer, Integer> returnedOrderItemQuantities,
        Instant occurredAt
) {

    public static CustomerReturnRefundFinalizedEvent now(
            Integer returnId,
            Integer orderId,
            Map<Integer, Integer> returnedOrderItemQuantities
    ) {
        return new CustomerReturnRefundFinalizedEvent(
                returnId,
                orderId,
                Map.copyOf(returnedOrderItemQuantities),
                Instant.now()
        );
    }
}
