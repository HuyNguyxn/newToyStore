package com.example.new_toy_store.global.event;

import java.time.Instant;

public record ProductReviewRatingChangedEvent(
        Integer productId,
        double averageRating,
        int reviewCount,
        Instant occurredAt
) {
    public static ProductReviewRatingChangedEvent now(Integer productId, double averageRating, int reviewCount) {
        return new ProductReviewRatingChangedEvent(productId, averageRating, reviewCount, Instant.now());
    }
}
