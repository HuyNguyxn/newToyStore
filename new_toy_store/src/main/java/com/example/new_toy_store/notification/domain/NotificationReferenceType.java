package com.example.new_toy_store.notification.domain;

import com.example.new_toy_store.notification.domain.exception.InvalidNotificationOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum NotificationReferenceType {
    ORDER("Order", "/orders/"),
    PAYMENT("Payment", "/payments/"),
    SHIPMENT("Shipment", "/shipments/"),
    CUSTOMER_RETURN("Customer return", "/customer-returns/"),
    REVIEW("Review", "/reviews/"),
    CART("Cart", "/cart"),
    SYSTEM("System", null);

    private final String displayName;
    private final String pathPrefix;

    NotificationReferenceType(String displayName, String pathPrefix) {
        this.displayName = displayName;
        this.pathPrefix = pathPrefix;
    }

    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String buildActionUrl(Integer referenceId) {
        if (pathPrefix == null) return null;
        if (this == CART) return pathPrefix;
        return referenceId == null ? null : pathPrefix + referenceId;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static NotificationReferenceType from(String value) {
        if (value == null || value.isBlank()) {
            throw InvalidNotificationOperationException.invalidReferenceType(value);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidNotificationOperationException.invalidReferenceType(value);
        }
    }
}
