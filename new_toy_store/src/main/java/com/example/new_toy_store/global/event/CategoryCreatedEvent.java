package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.Objects;

public record CategoryCreatedEvent(
        Integer categoryId,
        String slug,
        String path,
        Instant occurredAt
) {

    public CategoryCreatedEvent {
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        Objects.requireNonNull(slug, "slug must not be null");
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public static CategoryCreatedEvent now(Integer categoryId, String slug, String path) {
        return new CategoryCreatedEvent(categoryId, slug, path, Instant.now());
    }
}
