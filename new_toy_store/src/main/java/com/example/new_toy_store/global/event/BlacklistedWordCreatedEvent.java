package com.example.new_toy_store.global.event;

import com.example.new_toy_store.moderation.domain.WordCategory;

import java.time.Instant;

public record BlacklistedWordCreatedEvent(
        BlacklistedWordPayload payload,
        Instant occurredAt
) {
    public static BlacklistedWordCreatedEvent now(Integer wordId, String word, WordCategory category) {
        return new BlacklistedWordCreatedEvent(BlacklistedWordPayload.of(wordId, word, category), Instant.now());
    }
}
