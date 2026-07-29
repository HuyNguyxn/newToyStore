package com.example.new_toy_store.notification.application.dto.response;

import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import com.example.new_toy_store.notification.domain.NotificationStatus;
import com.example.new_toy_store.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationResponse(
        Integer id,
        NotificationType type,
        NotificationStatus status,
        String title,
        String message,
        NotificationReferenceType referenceType,
        Integer referenceId,
        String actionUrl,
        LocalDateTime occurredAt,
        LocalDateTime readAt,
        LocalDateTime expiresAt,
        List<NotificationActionResponse> allowedActions
) {
}
