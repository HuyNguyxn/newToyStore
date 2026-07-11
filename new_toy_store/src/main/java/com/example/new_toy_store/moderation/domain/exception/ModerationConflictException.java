package com.example.new_toy_store.moderation.domain.exception;

public class ModerationConflictException extends RuntimeException {
    private final String operation;
    private final Object conflictValue;

    private ModerationConflictException(String message, String operation, Object conflictValue) {
        super(message);
        this.operation = operation;
        this.conflictValue = conflictValue;
    }

    public static ModerationConflictException duplicateWord(String word) {
        return new ModerationConflictException("Từ khóa '" + word + "' đã tồn tại trong danh sách cấm (Đang hoạt động).", "ADD_OR_UPDATE_WORD", word);
    }

    public static ModerationConflictException deletedWordConflict(String word) {
        return new ModerationConflictException("Từ khóa '" + word + "' đã bị xóa mềm và đang nằm trong thùng rác. Vui lòng khôi phục (restore) thay vì thêm mới.", "ADD_WORD", word);
    }

    public String getOperation() { return operation; }
    public Object getConflictValue() { return conflictValue; }
}