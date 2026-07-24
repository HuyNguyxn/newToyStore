package com.example.new_toy_store.moderation.domain.exception;

import java.util.Map;

public class BlacklistedWordNotFoundException extends RuntimeException {
    private final Integer wordId;

    public BlacklistedWordNotFoundException(Integer wordId) {
        super("Không tìm thấy từ khóa cấm với mã: " + wordId);
        this.wordId = wordId;
    }

    public Map<String, ?> getContextData() {
        return Map.of("wordId", wordId);
    }

    public Integer getWordId() {
        return wordId;
    }
}
