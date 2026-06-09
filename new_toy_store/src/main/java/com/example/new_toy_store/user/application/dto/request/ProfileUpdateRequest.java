package com.example.new_toy_store.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phoneNumber;

    private String avatarUrl;

    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
}