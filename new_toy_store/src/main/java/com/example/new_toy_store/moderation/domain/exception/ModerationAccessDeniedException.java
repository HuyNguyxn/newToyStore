package com.example.new_toy_store.moderation.domain.exception;

import java.util.Map;

public class ModerationAccessDeniedException extends RuntimeException {
    private final String action;
    private final String reason;

    private ModerationAccessDeniedException(String message, String action, String reason) {
        super(message);
        this.action = action;
        this.reason = reason;
    }

    public static ModerationAccessDeniedException adminRequired(String action) {
        return new ModerationAccessDeniedException(
                "Từ chối truy cập: Bạn cần quyền quản trị viên để thực hiện hành động '" + action + "'.",
                action,
                "ADMIN_REQUIRED"
        );
    }

    public ModerationAccessDeniedException(String action) {
        this(
                "Từ chối truy cập: Bạn cần quyền quản trị viên để thực hiện hành động '" + action + "'.",
                action,
                "ADMIN_REQUIRED"
        );
    }

    public Map<String, ?> getContextData() {
        return Map.of(
                "action", action,
                "reason", reason
        );
    }

    public String getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }
}
