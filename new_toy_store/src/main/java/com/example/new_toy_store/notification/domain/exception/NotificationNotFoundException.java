package com.example.new_toy_store.notification.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotificationNotFoundException extends NotificationDomainException {

    public NotificationNotFoundException(Integer id) {
        super(
                HttpStatus.NOT_FOUND,
                "NOTIFICATION_NOT_FOUND",
                "Notification with ID " + id + " was not found.",
                Map.of("notificationId", id)
        );
    }
}
