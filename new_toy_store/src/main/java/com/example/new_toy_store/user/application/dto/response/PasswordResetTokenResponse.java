package com.example.new_toy_store.user.application.dto.response;

public class PasswordResetTokenResponse {

    private final String message;

    public PasswordResetTokenResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
