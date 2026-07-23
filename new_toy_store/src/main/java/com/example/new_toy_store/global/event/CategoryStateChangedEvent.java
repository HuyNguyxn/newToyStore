package com.example.new_toy_store.global.event;

import com.example.new_toy_store.category.domain.CategoryStatus;

import java.time.Instant;
import java.util.Objects;

public record CategoryStateChangedEvent(
        Integer categoryId,
        CategoryStatus previousStatus,
        CategoryStatus currentStatus,
        String path,
        Instant occurredAt
) {

    public CategoryStateChangedEvent {
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        Objects.requireNonNull(previousStatus, "previousStatus must not be null");
        Objects.requireNonNull(currentStatus, "currentStatus must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public static CategoryStateChangedEvent now(
            Integer categoryId,
            CategoryStatus previousStatus,
            CategoryStatus currentStatus,
            String path
    ) {
        return new CategoryStateChangedEvent(categoryId, previousStatus, currentStatus, path, Instant.now());
    }
}
