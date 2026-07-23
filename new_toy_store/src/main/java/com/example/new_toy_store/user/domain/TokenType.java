package com.example.new_toy_store.user.domain;

import com.example.new_toy_store.user.domain.exception.InvalidUserOperationException;

public enum TokenType {

    VERIFICATION("Xác thực Email", 15),
    RESET_PASSWORD("Khôi phục mật khẩu", 10),
    ACCESS_TOKEN("Token đăng nhập", 1440);

    private final String displayName;
    private final int expirationMinutes;

    TokenType(String displayName, int expirationMinutes) {
        this.displayName = displayName;
        this.expirationMinutes = expirationMinutes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getExpirationMinutes() {
        return expirationMinutes;
    }

    public static TokenType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidUserOperationException.inputDataInvalid("tokenType", "Loại token không được để trống");
        }
        try {
            return TokenType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidUserOperationException.invalidTokenType(value);
        }
    }
}
