package com.example.new_toy_store.global.event;

import java.time.Instant;

public record BlacklistedWordDeletedEvent(
        Integer wordId,
        String word,
        DeleteMode deleteMode,
        Instant occurredAt
) {
    public enum DeleteMode {
        SOFT_DELETE,
        HARD_DELETE
    }

    public static BlacklistedWordDeletedEvent softDeleted(Integer wordId, String word) {
        return new BlacklistedWordDeletedEvent(wordId, word, DeleteMode.SOFT_DELETE, Instant.now());
    }

    public static BlacklistedWordDeletedEvent hardDeleted(Integer wordId, String word) {
        return new BlacklistedWordDeletedEvent(wordId, word, DeleteMode.HARD_DELETE, Instant.now());
    }
}
