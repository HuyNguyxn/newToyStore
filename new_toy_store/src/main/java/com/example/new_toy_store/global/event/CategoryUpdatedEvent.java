package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.Objects;

public record CategoryUpdatedEvent(
        Integer categoryId,
        String oldPath,
        String newPath,
        boolean pathChanged,
        Instant occurredAt
) {

    public CategoryUpdatedEvent {
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public static CategoryUpdatedEvent now(Integer categoryId, String oldPath, String newPath, boolean pathChanged) {
        return new CategoryUpdatedEvent(categoryId, oldPath, newPath, pathChanged, Instant.now());
    }
}
