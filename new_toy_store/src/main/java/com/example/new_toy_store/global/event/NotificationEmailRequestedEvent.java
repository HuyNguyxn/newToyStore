package com.example.new_toy_store.global.event;

import com.example.new_toy_store.notification.domain.NotificationType;

import java.time.Instant;

public record NotificationEmailRequestedEvent(
        Integer notificationId,
        Integer recipientUserId,
        NotificationType type,
        String title,
        String message,
        Instant occurredAt
) {
    public static NotificationEmailRequestedEvent now(
            Integer notificationId,
            Integer recipientUserId,
            NotificationType type,
            String title,
            String message
    ) {
        return new NotificationEmailRequestedEvent(
                notificationId,
                recipientUserId,
                type,
                title,
                message,
                Instant.now()
        );
    }
}
