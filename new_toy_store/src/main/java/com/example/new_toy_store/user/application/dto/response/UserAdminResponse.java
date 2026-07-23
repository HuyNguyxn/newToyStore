package com.example.new_toy_store.user.application.dto.response;

import com.example.new_toy_store.user.domain.UserRole;
import com.example.new_toy_store.user.domain.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public class UserAdminResponse {

    private Integer id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private String roleDisplayName;
    private UserRole roleDetail;
    private String status;
    private String statusDisplayName;
    private UserStatus statusDetail;
    private List<UserStatus> allowedNextStatuses;
    private List<UserRole> assignableRoles;
    private List<String> allowedActions;
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
            UserRole roleDetail,
            String status,
            String statusDisplayName,
            UserStatus statusDetail,
            List<UserStatus> allowedNextStatuses,
            List<UserRole> assignableRoles,
            List<String> allowedActions,
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
        this.roleDetail = roleDetail;
        this.status = status;
        this.statusDisplayName = statusDisplayName;
        this.statusDetail = statusDetail;
        this.allowedNextStatuses = allowedNextStatuses;
        this.assignableRoles = assignableRoles;
        this.allowedActions = allowedActions;
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
    public UserRole getRoleDetail() { return roleDetail; }
    public String getStatus() { return status; }
    public String getStatusDisplayName() { return statusDisplayName; }
    public UserStatus getStatusDetail() { return statusDetail; }
    public List<UserStatus> getAllowedNextStatuses() { return allowedNextStatuses; }
    public List<UserRole> getAssignableRoles() { return assignableRoles; }
    public List<String> getAllowedActions() { return allowedActions; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
