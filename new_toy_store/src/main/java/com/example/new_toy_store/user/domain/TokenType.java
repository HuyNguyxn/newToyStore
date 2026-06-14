package com.example.new_toy_store.user.domain;

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
}