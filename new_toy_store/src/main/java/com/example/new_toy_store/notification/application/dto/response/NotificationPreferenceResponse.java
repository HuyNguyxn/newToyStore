package com.example.new_toy_store.notification.application.dto.response;

public record NotificationPreferenceResponse(
        boolean inAppEnabled,
        boolean emailEnabled,
        boolean orderEnabled,
        boolean paymentEnabled,
        boolean shipmentEnabled,
        boolean returnEnabled,
        boolean reviewEnabled,
        boolean cartEnabled,
        boolean systemEnabled
) {
}
