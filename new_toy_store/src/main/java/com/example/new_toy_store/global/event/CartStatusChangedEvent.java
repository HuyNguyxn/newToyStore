package com.example.new_toy_store.global.event;

import com.example.new_toy_store.cart.domain.CartStatus;

import java.time.Instant;
import java.util.Objects;

public record CartStatusChangedEvent(
        Integer cartId,
        Integer userId,
        CartStatus previousStatus,
        CartStatus currentStatus,
        Instant occurredAt
) {

    public CartStatusChangedEvent {
        Objects.requireNonNull(cartId, "cartId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(previousStatus, "previousStatus must not be null");
        Objects.requireNonNull(currentStatus, "currentStatus must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        if (previousStatus == currentStatus) {
            throw new IllegalArgumentException("A status change event requires different statuses");
        }
    }

    public static CartStatusChangedEvent now(
            Integer cartId,
            Integer userId,
            CartStatus previousStatus,
            CartStatus currentStatus
    ) {
        return new CartStatusChangedEvent(cartId, userId, previousStatus, currentStatus, Instant.now());
    }
}
