package com.example.new_toy_store.user.application.dto.response;

import com.example.new_toy_store.user.domain.UserRole;
import com.example.new_toy_store.user.domain.UserStatus;

import java.util.List;

public class UserProfileResponse {

    private Integer id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private UserRole roleDetail;
    private String status;
    private UserStatus statusDetail;
    private List<UserStatus> allowedNextStatuses;
    private List<String> allowedActions;
    private List<AddressResponse> addresses;

    public UserProfileResponse(
            Integer id,
            String email,
            String fullName,
            String phoneNumber,
            String avatarUrl,
            String role,
            UserRole roleDetail,
            String status,
            UserStatus statusDetail,
            List<UserStatus> allowedNextStatuses,
            List<String> allowedActions,
            List<AddressResponse> addresses
    ) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.roleDetail = roleDetail;
        this.status = status;
        this.statusDetail = statusDetail;
        this.allowedNextStatuses = allowedNextStatuses;
        this.allowedActions = allowedActions;
        this.addresses = addresses;
    }

    public Integer getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role; }
    public UserRole getRoleDetail() { return roleDetail; }
    public String getStatus() { return status; }
    public UserStatus getStatusDetail() { return statusDetail; }
    public List<UserStatus> getAllowedNextStatuses() { return allowedNextStatuses; }
    public List<String> getAllowedActions() { return allowedActions; }
    public List<AddressResponse> getAddresses() { return addresses; }
}
