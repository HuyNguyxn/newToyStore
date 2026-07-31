package com.example.new_toy_store.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    private String phoneNumber;

    @Size(max = 1000, message = "URL avatar không được vượt quá 1000 ký tự")
    @Pattern(regexp = "^(|https?://.+|/.*)$", message = "URL avatar phải là đường dẫn http(s) hoặc đường dẫn nội bộ bắt đầu bằng /")
    private String avatarUrl;

    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
}
