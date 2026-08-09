package com.example.new_toy_store.user.application.dto.response;

import java.time.LocalDateTime;

public record DeletedUserAdminResponse(
        Integer id,
        String email,
        String fullName,
        String phoneNumber,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}
