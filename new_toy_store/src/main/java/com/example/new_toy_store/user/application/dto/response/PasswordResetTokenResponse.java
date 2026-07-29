package com.example.new_toy_store.user.application.dto.response;

import java.time.LocalDateTime;

public class PasswordResetTokenResponse {

    private final String email;
    private final String token;
    private final LocalDateTime expiresAt;
    private final String message;

    public PasswordResetTokenResponse(String email, String token, LocalDateTime expiresAt, String message) {
        this.email = email;
        this.token = token;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getMessage() {
        return message;
    }
}
