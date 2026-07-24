package com.example.new_toy_store.moderation.domain.exception;

import java.util.Map;

public class ModerationConflictException extends RuntimeException {
    private final String operation;
    private final Object conflictValue;
    private final String conflictType;

    private ModerationConflictException(String message, String operation, Object conflictValue, String conflictType) {
        super(message);
        this.operation = operation;
        this.conflictValue = conflictValue;
        this.conflictType = conflictType;
    }

    public static ModerationConflictException duplicateWord(String word) {
        return new ModerationConflictException(
                "Từ khóa '" + word + "' đã tồn tại trong danh sách cấm đang hoạt động.",
                "ADD_OR_UPDATE_WORD",
                word,
                "ACTIVE_DUPLICATE"
        );
    }

    public static ModerationConflictException deletedWordConflict(String word) {
        return new ModerationConflictException(
                "Từ khóa '" + word + "' đã bị xóa mềm. Vui lòng khôi phục thay vì thêm mới.",
                "ADD_WORD",
                word,
                "SOFT_DELETED_CONFLICT"
        );
    }

    public Map<String, ?> getContextData() {
        return Map.of(
                "operation", operation,
                "conflictValue", conflictValue,
                "conflictType", conflictType
        );
    }

    public String getOperation() {
        return operation;
    }

    public Object getConflictValue() {
        return conflictValue;
    }

    public String getConflictType() {
        return conflictType;
    }
}
