package com.example.new_toy_store.notification.mapper;

import com.example.new_toy_store.notification.application.dto.response.NotificationActionResponse;
import com.example.new_toy_store.notification.application.dto.response.NotificationPreferenceResponse;
import com.example.new_toy_store.notification.application.dto.response.NotificationResponse;
import com.example.new_toy_store.notification.domain.Notification;
import com.example.new_toy_store.notification.domain.NotificationPreference;
import com.example.new_toy_store.notification.domain.NotificationStatus;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getStatus(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getActionUrl(),
                notification.getOccurredAt(),
                notification.getReadAt(),
                notification.getExpiresAt(),
                notification.getStatus().getNextValidStates().stream()
                        .map(NotificationMapper::toActionResponse)
                        .toList()
        );
    }

    public static NotificationPreferenceResponse toResponse(NotificationPreference preference) {
        return new NotificationPreferenceResponse(
                preference.isInAppEnabled(),
                preference.isEmailEnabled(),
                preference.isOrderEnabled(),
                preference.isPaymentEnabled(),
                preference.isShipmentEnabled(),
                preference.isReturnEnabled(),
                preference.isReviewEnabled(),
                preference.isCartEnabled(),
                preference.isSystemEnabled()
        );
    }

    private static NotificationActionResponse toActionResponse(NotificationStatus status) {
        return new NotificationActionResponse(status.getCode(), status.getDisplayName());
    }
}
