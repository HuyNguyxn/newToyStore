package com.example.new_toy_store.notification.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotificationAccessDeniedException extends NotificationDomainException {

    public NotificationAccessDeniedException(Integer notificationId, Integer userId) {
        super(
                HttpStatus.FORBIDDEN,
                "NOTIFICATION_ACCESS_DENIED",
                "You do not have permission to access this notification.",
                Map.of("notificationId", notificationId, "userId", userId)
        );
    }
}
