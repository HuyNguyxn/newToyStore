package com.example.new_toy_store.moderation.domain.exception;

public class InvalidModerationOperationException extends RuntimeException {
    private final String operation;
    private final Object invalidValue;

    private InvalidModerationOperationException(String message, String operation, Object invalidValue) {
        super(message);
        this.operation = operation;
        this.invalidValue = invalidValue;
    }

    public static InvalidModerationOperationException emptyWord() {
        return new InvalidModerationOperationException(
                "Từ khóa cấm không được để trống.",
                "VALIDATE_WORD",
                null
        );
    }

    public static InvalidModerationOperationException nullCategory() {
        return new InvalidModerationOperationException(
                "Loại từ khóa không được để trống.",
                "VALIDATE_CATEGORY",
                null
        );
    }

    public static InvalidModerationOperationException invalidCategory(String invalidValue, String acceptedValues) {
        return new InvalidModerationOperationException(
                "Loại từ khóa không hợp lệ: '" + invalidValue + "'. Chỉ chấp nhận: " + acceptedValues,
                "PARSE_CATEGORY",
                invalidValue
        );
    }

    public String getOperation() {
        return operation;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}
