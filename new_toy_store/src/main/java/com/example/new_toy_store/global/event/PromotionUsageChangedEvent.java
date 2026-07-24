package com.example.new_toy_store.global.event;

import java.time.Instant;

public record PromotionUsageChangedEvent(
        Integer promotionId,
        String code,
        int previousUsedCount,
        int currentUsedCount,
        String action,
        Instant occurredAt
) {
    public static PromotionUsageChangedEvent now(
            Integer promotionId,
            String code,
            int previousUsedCount,
            int currentUsedCount,
            String action
    ) {
        return new PromotionUsageChangedEvent(
                promotionId,
                code,
                previousUsedCount,
                currentUsedCount,
                action,
                Instant.now()
        );
    }
}
