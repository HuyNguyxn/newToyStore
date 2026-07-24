package com.example.new_toy_store.moderation.domain.exception;

import java.util.HashMap;
import java.util.Map;

public class InvalidModerationOperationException extends RuntimeException {
    private final String operation;
    private final Object invalidValue;
    private final String errorType;

    private InvalidModerationOperationException(String message,
                                                String operation,
                                                Object invalidValue,
                                                String errorType) {
        super(message);
        this.operation = operation;
        this.invalidValue = invalidValue;
        this.errorType = errorType;
    }

    public static InvalidModerationOperationException emptyWord() {
        return new InvalidModerationOperationException(
                "Từ khóa cấm không được để trống.",
                "VALIDATE_WORD",
                null,
                "EMPTY_WORD"
        );
    }

    public static InvalidModerationOperationException nullCategory() {
        return new InvalidModerationOperationException(
                "Loại từ khóa không được để trống.",
                "VALIDATE_CATEGORY",
                null,
                "EMPTY_CATEGORY"
        );
    }

    public static InvalidModerationOperationException invalidCategory(String invalidValue, String acceptedValues) {
        return new InvalidModerationOperationException(
                "Loại từ khóa không hợp lệ: '" + invalidValue + "'. Chỉ chấp nhận: " + acceptedValues,
                "PARSE_CATEGORY",
                invalidValue,
                "INVALID_CATEGORY"
        );
    }

    public Map<String, ?> getContextData() {
        Map<String, Object> context = new HashMap<>();
        context.put("operation", operation);
        context.put("invalidValue", invalidValue);
        context.put("errorType", errorType);
        return context;
    }

    public String getOperation() {
        return operation;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    public String getErrorType() {
        return errorType;
    }
}
