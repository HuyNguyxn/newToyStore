package com.example.new_toy_store.moderation.domain;

import com.example.new_toy_store.moderation.domain.exception.InvalidModerationOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum WordCategory {
    PROFANITY("PROFANITY", "Từ ngữ thô tục, xúc phạm", 3) {
        @Override
        public boolean requiresImmediateHide() {
            return true;
        }

        @Override
        public String getSystemNote() {
            return "Tự động khóa hiển thị do chứa ngôn từ thô tục.";
        }
    },
    SPAM("SPAM", "Link quảng cáo, rác", 2) {
        @Override
        public boolean requiresImmediateHide() {
            return true;
        }

        @Override
        public String getSystemNote() {
            return "Tự động khóa hiển thị do phát hiện dấu hiệu spam, lừa đảo.";
        }
    },
    COMPETITOR("COMPETITOR", "Tên thương hiệu đối thủ", 1) {
        @Override
        public boolean requiresImmediateHide() {
            return false;
        }

        @Override
        public String getSystemNote() {
            return "Cần Admin kiểm duyệt thủ công do nhắc đến thương hiệu khác.";
        }
    },
    OTHER("OTHER", "Khác", 0) {
        @Override
        public boolean requiresImmediateHide() {
            return false;
        }

        @Override
        public String getSystemNote() {
            return "Cần Admin kiểm duyệt thủ công vì lý do khác.";
        }
    };

    private final String code;
    private final String description;
    private final int severityLevel;

    WordCategory(String code, String description, int severityLevel) {
        this.code = code;
        this.description = description;
        this.severityLevel = severityLevel;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public abstract boolean requiresImmediateHide();

    public abstract String getSystemNote();

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static WordCategory from(Object input) {
        if (input == null) {
            throw InvalidModerationOperationException.nullCategory();
        }

        if (input instanceof Map<?, ?> objectValue) {
            Object codeValue = objectValue.get("code");
            if (codeValue == null) {
                codeValue = objectValue.get("name");
            }
            if (codeValue == null) {
                codeValue = objectValue.get("category");
            }
            return fromText(String.valueOf(codeValue));
        }

        return fromText(String.valueOf(input));
    }

    public static WordCategory from(String value) {
        return fromText(value);
    }

    private static WordCategory fromText(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            throw InvalidModerationOperationException.nullCategory();
        }

        String normalized = value.trim().toUpperCase();
        for (WordCategory category : values()) {
            if (category.name().equals(normalized) || category.code.equalsIgnoreCase(value.trim())) {
                return category;
            }
        }

        String acceptedValues = Arrays.stream(values())
                .map(WordCategory::getCode)
                .collect(Collectors.joining(", "));
        throw InvalidModerationOperationException.invalidCategory(value, acceptedValues);
    }
}
