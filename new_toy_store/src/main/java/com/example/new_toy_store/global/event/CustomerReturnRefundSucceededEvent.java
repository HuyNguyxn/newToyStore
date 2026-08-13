package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.Map;

public record CustomerReturnRefundSucceededEvent(
        Integer returnId,
        Integer orderId,
        Map<Integer, Integer> returnedOrderItemQuantities,
        Instant occurredAt
) {
    public static CustomerReturnRefundSucceededEvent now(
            Integer returnId,
            Integer orderId,
            Map<Integer, Integer> returnedOrderItemQuantities
    ) {
        return new CustomerReturnRefundSucceededEvent(
                returnId,
                orderId,
                Map.copyOf(returnedOrderItemQuantities),
                Instant.now()
        );
    }
}
