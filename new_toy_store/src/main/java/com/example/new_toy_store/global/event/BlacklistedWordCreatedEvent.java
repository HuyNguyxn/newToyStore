package com.example.new_toy_store.global.event;

import com.example.new_toy_store.moderation.domain.WordCategory;

import java.time.Instant;

public record BlacklistedWordCreatedEvent(
        Integer wordId,
        String word,
        WordCategory category,
        Instant occurredAt
) {
    public static BlacklistedWordCreatedEvent now(Integer wordId, String word, WordCategory category) {
        return new BlacklistedWordCreatedEvent(wordId, word, category, Instant.now());
    }
}
