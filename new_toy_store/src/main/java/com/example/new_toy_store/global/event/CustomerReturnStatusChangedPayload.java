package com.example.new_toy_store.global.event;

import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;

public record CustomerReturnStatusChangedPayload(
        Integer returnId,
        Integer orderId,
        Integer userId,
        CustomerReturnStatus previousStatus,
        CustomerReturnStatus currentStatus,
        String actionBy
) {
    public static CustomerReturnStatusChangedPayload of(Integer returnId,
                                                        Integer orderId,
                                                        Integer userId,
                                                        CustomerReturnStatus previousStatus,
                                                        CustomerReturnStatus currentStatus,
                                                        String actionBy) {
        return new CustomerReturnStatusChangedPayload(returnId, orderId, userId, previousStatus, currentStatus, actionBy);
    }
}
