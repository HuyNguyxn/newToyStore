package com.example.new_toy_store.user.domain;

import java.time.LocalDateTime;

public interface DeletedUserProjection {
    Integer getId();
    String getEmail();
    String getFullName();
    String getPhoneNumber();
    String getRole();
    String getStatus();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    LocalDateTime getDeletedAt();
}
