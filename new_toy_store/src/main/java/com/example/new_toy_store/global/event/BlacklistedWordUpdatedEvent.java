package com.example.new_toy_store.global.event;

import com.example.new_toy_store.moderation.domain.WordCategory;

import java.time.Instant;

public record BlacklistedWordUpdatedEvent(
        BlacklistedWordPayload previous,
        BlacklistedWordPayload current,
        Instant occurredAt
) {
    public static BlacklistedWordUpdatedEvent now(Integer wordId,
                                                  String previousWord,
                                                  String currentWord,
                                                  WordCategory category) {
        return new BlacklistedWordUpdatedEvent(
                BlacklistedWordPayload.of(wordId, previousWord, category),
                BlacklistedWordPayload.of(wordId, currentWord, category),
                Instant.now()
        );
    }
}
