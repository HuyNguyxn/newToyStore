package com.example.new_toy_store.notification.domain;

import com.example.new_toy_store.notification.domain.exception.InvalidNotificationOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum NotificationType {
    ORDER_CREATED("Order created", NotificationCategory.ORDER, false),
    ORDER_STATUS_CHANGED("Order status changed", NotificationCategory.ORDER, true),
    ORDER_CANCELLED("Order cancelled", NotificationCategory.ORDER, true),
    PAYMENT_COMPLETED("Payment completed", NotificationCategory.PAYMENT, true),
    PAYMENT_FAILED("Payment failed", NotificationCategory.PAYMENT, true),
    PAYMENT_REFUNDED("Payment refunded", NotificationCategory.PAYMENT, true),
    SHIPMENT_CREATED("Shipment created", NotificationCategory.SHIPMENT, false),
    SHIPMENT_IN_TRANSIT("Shipment in transit", NotificationCategory.SHIPMENT, false),
    SHIPMENT_DELIVERED("Shipment delivered", NotificationCategory.SHIPMENT, true),
    SHIPMENT_RETURNED("Shipment returned", NotificationCategory.SHIPMENT, true),
    SHIPMENT_CANCELLED("Shipment cancelled", NotificationCategory.SHIPMENT, true),
    RETURN_STATUS_CHANGED("Return status changed", NotificationCategory.RETURN, true),
    RETURN_REFUNDED("Return refunded", NotificationCategory.RETURN, true),
    REVIEW_REPLIED("Review replied", NotificationCategory.REVIEW, false),
    REVIEW_STATUS_CHANGED("Review status changed", NotificationCategory.REVIEW, false),
    CART_EXPIRING("Cart item expiring", NotificationCategory.CART, false),
    SYSTEM_ANNOUNCEMENT("System announcement", NotificationCategory.SYSTEM, true);

    private final String displayName;
    private final NotificationCategory category;
    private final boolean emailSupported;

    NotificationType(String displayName, NotificationCategory category, boolean emailSupported) {
        this.displayName = displayName;
        this.category = category;
        this.emailSupported = emailSupported;
    }

    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public boolean isEmailSupported() {
        return emailSupported;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static NotificationType from(String value) {
        if (value == null || value.isBlank()) {
            throw InvalidNotificationOperationException.invalidType(value);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidNotificationOperationException.invalidType(value);
        }
    }
}
