package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.Objects;

public record UserDeletedEvent(
        Integer userId,
        String email,
        Instant occurredAt
) {

    public UserDeletedEvent {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public static UserDeletedEvent now(Integer userId, String email) {
        return new UserDeletedEvent(userId, email, Instant.now());
    }
}
