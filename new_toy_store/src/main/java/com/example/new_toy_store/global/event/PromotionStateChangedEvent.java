package com.example.new_toy_store.global.event;

import java.time.Instant;

public record PromotionStateChangedEvent(
        Integer promotionId,
        String code,
        boolean previousActive,
        boolean currentActive,
        String action,
        Instant occurredAt
) {
    public static PromotionStateChangedEvent now(
            Integer promotionId,
            String code,
            boolean previousActive,
            boolean currentActive,
            String action
    ) {
        return new PromotionStateChangedEvent(
                promotionId,
                code,
                previousActive,
                currentActive,
                action,
                Instant.now()
        );
    }
}
