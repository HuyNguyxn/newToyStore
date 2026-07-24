package com.example.new_toy_store.global.event;

import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;

import java.time.Instant;

public record CustomerReturnStatusChangedEvent(
        Integer returnId,
        Integer orderId,
        CustomerReturnStatus previousStatus,
        CustomerReturnStatus currentStatus,
        String actionBy,
        Instant occurredAt
) {
    public static CustomerReturnStatusChangedEvent now(Integer returnId,
                                                       Integer orderId,
                                                       CustomerReturnStatus previousStatus,
                                                       CustomerReturnStatus currentStatus,
                                                       String actionBy) {
        return new CustomerReturnStatusChangedEvent(
                returnId,
                orderId,
                previousStatus,
                currentStatus,
                actionBy,
                Instant.now()
        );
    }
}
