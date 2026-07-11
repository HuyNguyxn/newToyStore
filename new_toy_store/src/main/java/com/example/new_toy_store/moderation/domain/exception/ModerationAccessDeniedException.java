package com.example.new_toy_store.moderation.domain.exception;

public class ModerationAccessDeniedException extends RuntimeException {
    private final String action;
    public ModerationAccessDeniedException(String action) {
        super("Từ chối truy cập: Bạn không có quyền thực hiện hành động '" + action + "'. Yêu cầu quyền Quản trị viên.");
        this.action = action;
    }
    public String getAction() { return action; }
}