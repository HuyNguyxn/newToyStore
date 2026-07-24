package com.example.new_toy_store.global.event;

import com.example.new_toy_store.moderation.domain.WordCategory;

import java.time.Instant;

public record BlacklistedWordUpdatedEvent(
        Integer wordId,
        String previousWord,
        String currentWord,
        WordCategory category,
        Instant occurredAt
) {
    public static BlacklistedWordUpdatedEvent now(Integer wordId,
                                                  String previousWord,
                                                  String currentWord,
                                                  WordCategory category) {
        return new BlacklistedWordUpdatedEvent(wordId, previousWord, currentWord, category, Instant.now());
    }
}
