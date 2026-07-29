package com.example.new_toy_store.notification.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotificationDeletedConflictException extends NotificationDomainException {

    public NotificationDeletedConflictException(Integer id) {
        super(
                HttpStatus.CONFLICT,
                "NOTIFICATION_DELETED_CONFLICT",
                "This notification was deleted or expired, so the operation cannot continue.",
                Map.of("notificationId", id)
        );
    }
}
