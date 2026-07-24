package com.example.new_toy_store.global.event;

import com.example.new_toy_store.moderation.domain.WordCategory;

import java.time.Instant;

public record BlacklistedWordRestoredEvent(
        BlacklistedWordPayload payload,
        Instant occurredAt
) {
    public static BlacklistedWordRestoredEvent now(Integer wordId, String word, WordCategory category) {
        return new BlacklistedWordRestoredEvent(BlacklistedWordPayload.of(wordId, word, category), Instant.now());
    }
}
