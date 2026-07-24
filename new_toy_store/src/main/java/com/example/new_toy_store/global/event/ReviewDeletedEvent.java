package com.example.new_toy_store.global.event;

import java.time.Instant;

public record ReviewDeletedEvent(
        Integer reviewId,
        Integer userId,
        Integer productId,
        Instant occurredAt
) {
    public static ReviewDeletedEvent now(Integer reviewId, Integer userId, Integer productId) {
        return new ReviewDeletedEvent(reviewId, userId, productId, Instant.now());
    }
}
