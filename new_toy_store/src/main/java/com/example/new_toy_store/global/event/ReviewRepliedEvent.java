package com.example.new_toy_store.global.event;

import java.time.Instant;

public record ReviewRepliedEvent(
        Integer reviewId,
        Integer userId,
        Integer productId,
        Instant occurredAt
) {
    public static ReviewRepliedEvent now(Integer reviewId, Integer userId, Integer productId) {
        return new ReviewRepliedEvent(reviewId, userId, productId, Instant.now());
    }
}
