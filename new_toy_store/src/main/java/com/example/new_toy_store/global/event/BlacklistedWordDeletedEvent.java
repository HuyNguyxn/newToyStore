package com.example.new_toy_store.global.event;

import com.example.new_toy_store.moderation.domain.WordCategory;

import java.time.Instant;

public record BlacklistedWordDeletedEvent(
        BlacklistedWordPayload payload,
        DeleteMode deleteMode,
        Instant occurredAt
) {
    public enum DeleteMode {
        SOFT_DELETE,
        HARD_DELETE
    }

    public static BlacklistedWordDeletedEvent softDeleted(Integer wordId, String word, WordCategory category) {
        return new BlacklistedWordDeletedEvent(
                BlacklistedWordPayload.of(wordId, word, category),
                DeleteMode.SOFT_DELETE,
                Instant.now()
        );
    }

    public static BlacklistedWordDeletedEvent hardDeleted(Integer wordId, String word, WordCategory category) {
        return new BlacklistedWordDeletedEvent(
                BlacklistedWordPayload.of(wordId, word, category),
                DeleteMode.HARD_DELETE,
                Instant.now()
        );
    }
}
