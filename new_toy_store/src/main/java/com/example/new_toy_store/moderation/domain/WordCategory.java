package com.example.new_toy_store.moderation.domain;

import com.example.new_toy_store.moderation.domain.exception.InvalidModerationOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum WordCategory {
    PROFANITY("Từ ngữ thô tục, xúc phạm", 3) {
        @Override
        public boolean requiresImmediateHide() { return true; }
        @Override
        public String getSystemNote() { return "Tự động khóa hiển thị do chứa ngôn từ thô tục."; }
    },
    SPAM("Link quảng cáo, rác", 2) {
        @Override
        public boolean requiresImmediateHide() { return true; }
        @Override
        public String getSystemNote() { return "Tự động khóa hiển thị do phát hiện dấu hiệu spam, lừa đảo."; }
    },
    COMPETITOR("Tên thương hiệu đối thủ", 1) {
        @Override
        public boolean requiresImmediateHide() { return false; }
        @Override
        public String getSystemNote() { return "Cần Admin kiểm duyệt thủ công do nhắc đến thương hiệu khác."; }
    },
    OTHER("Khác", 0) {
        @Override
        public boolean requiresImmediateHide() { return false; }
        @Override
        public String getSystemNote() { return "Cần Admin kiểm duyệt thủ công (Lý do khác)."; }
    };

    private final String description;
    private final int severityLevel;

    WordCategory(String description, int severityLevel) {
        this.description = description;
        this.severityLevel = severityLevel;
    }

    public String getDescription() { return description; }
    public int getSeverityLevel() { return severityLevel; }

    public abstract boolean requiresImmediateHide();
    public abstract String getSystemNote();

    @JsonCreator
    public static WordCategory from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidModerationOperationException.nullCategory();
        }
        try {
            return WordCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String acceptedValues = Arrays.stream(values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw InvalidModerationOperationException.invalidCategory(value, acceptedValues);
        }
    }
}