package com.example.new_toy_store.global.event;

import com.example.new_toy_store.review.domain.ReviewStatus;

import java.time.Instant;

public record ReviewStatusChangedEvent(
        Integer reviewId,
        Integer userId,
        Integer productId,
        ReviewStatus previousStatus,
        ReviewStatus currentStatus,
        Instant occurredAt
) {
    public static ReviewStatusChangedEvent now(
            Integer reviewId,
            Integer userId,
            Integer productId,
            ReviewStatus previousStatus,
            ReviewStatus currentStatus
    ) {
        return new ReviewStatusChangedEvent(
                reviewId,
                userId,
                productId,
                previousStatus,
                currentStatus,
                Instant.now()
        );
    }
}
