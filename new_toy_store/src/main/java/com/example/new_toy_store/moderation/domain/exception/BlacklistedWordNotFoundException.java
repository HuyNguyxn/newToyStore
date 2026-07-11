package com.example.new_toy_store.moderation.domain.exception;

public class BlacklistedWordNotFoundException extends RuntimeException {
    private final Integer wordId;
    public BlacklistedWordNotFoundException(Integer wordId) {
        super("Không tìm thấy từ khóa cấm (Word ID: " + wordId + ").");
        this.wordId = wordId;
    }
    public Integer getWordId() { return wordId; }
}