package com.example.new_toy_store.notification.domain;

import com.example.new_toy_store.notification.domain.exception.InvalidNotificationOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum NotificationStatus {

    UNREAD("Unread") {
        @Override
        public List<NotificationStatus> getNextValidStates() {
            return List.of(READ, ARCHIVED);
        }
    },
    READ("Read") {
        @Override
        public List<NotificationStatus> getNextValidStates() {
            return List.of(ARCHIVED);
        }
    },
    ARCHIVED("Archived") {
        @Override
        public List<NotificationStatus> getNextValidStates() {
            return List.of();
        }
    };

    private final String displayName;

    NotificationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract List<NotificationStatus> getNextValidStates();

    public boolean canTransitionTo(NotificationStatus target) {
        return target != null && getNextValidStates().contains(target);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static NotificationStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw InvalidNotificationOperationException.invalidStatus(value);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidNotificationOperationException.invalidStatus(value);
        }
    }
}
