package com.example.new_toy_store.notification.domain.exception;

import com.example.new_toy_store.notification.domain.NotificationStatus;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class InvalidNotificationOperationException extends NotificationDomainException {

    private InvalidNotificationOperationException(String code, String message, Map<String, Object> context) {
        super(HttpStatus.BAD_REQUEST, code, message, context);
    }

    public static InvalidNotificationOperationException invalidStatus(String value) {
        return invalidEnum("NOTIFICATION_STATUS_INVALID", "status", value,
                Arrays.stream(NotificationStatus.values()).map(Enum::name).toList());
    }

    public static InvalidNotificationOperationException invalidType(String value) {
        return invalidEnum("NOTIFICATION_TYPE_INVALID", "type", value, null);
    }

    public static InvalidNotificationOperationException invalidReferenceType(String value) {
        return invalidEnum("NOTIFICATION_REFERENCE_TYPE_INVALID", "referenceType", value, null);
    }

    public static InvalidNotificationOperationException invalidTransition(
            Integer notificationId,
            NotificationStatus current,
            NotificationStatus target
    ) {
        return new InvalidNotificationOperationException(
                "NOTIFICATION_TRANSITION_INVALID",
                "Cannot move notification from " + current.getDisplayName() + " to " + target.getDisplayName() + ".",
                Map.of("notificationId", notificationId, "currentStatus", current.name(), "targetStatus", target.name())
        );
    }

    public static InvalidNotificationOperationException invalidData(String field, Object value) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("field", field);
        context.put("rejectedValue", value);
        return new InvalidNotificationOperationException(
                "NOTIFICATION_DATA_INVALID",
                "Notification data is invalid at field " + field + ".",
                context
        );
    }

    private static InvalidNotificationOperationException invalidEnum(
            String code,
            String field,
            String value,
            Object allowedValues
    ) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("field", field);
        context.put("rejectedValue", value);
        if (allowedValues != null) context.put("allowedValues", allowedValues);
        return new InvalidNotificationOperationException(
                code,
                "Invalid " + field + " value: " + value + ".",
                context
        );
    }
}
