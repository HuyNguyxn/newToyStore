package com.example.new_toy_store.user.application.dto.response;

import java.time.LocalDateTime;

public class UserAdminResponse {

    private Integer id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private String roleDisplayName;
    private String status;
    private String statusDisplayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserAdminResponse(
            Integer id,
            String email,
            String fullName,
            String phoneNumber,
            String avatarUrl,
            String role,
            String roleDisplayName,
            String status,
            String statusDisplayName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.roleDisplayName = roleDisplayName;
        this.status = status;
        this.statusDisplayName = statusDisplayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role; }
    public String getRoleDisplayName() { return roleDisplayName; }
    public String getStatus() { return status; }
    public String getStatusDisplayName() { return statusDisplayName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
