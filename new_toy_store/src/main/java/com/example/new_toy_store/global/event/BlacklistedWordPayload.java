package com.example.new_toy_store.global.event;

import com.example.new_toy_store.moderation.domain.WordCategory;

public record BlacklistedWordPayload(
        Integer wordId,
        String word,
        WordCategory category
) {
    public static BlacklistedWordPayload of(Integer wordId, String word, WordCategory category) {
        return new BlacklistedWordPayload(wordId, word, category);
    }
}
