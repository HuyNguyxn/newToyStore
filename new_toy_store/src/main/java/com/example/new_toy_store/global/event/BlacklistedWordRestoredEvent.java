package com.example.new_toy_store.global.event;

import java.time.Instant;

public record BlacklistedWordRestoredEvent(
        Integer wordId,
        String word,
        Instant occurredAt
) {
    public static BlacklistedWordRestoredEvent now(Integer wordId, String word) {
        return new BlacklistedWordRestoredEvent(wordId, word, Instant.now());
    }
}
