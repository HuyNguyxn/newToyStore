package com.example.new_toy_store.user.application.dto.response;

import java.util.List;

public class UserProfileResponse {

    private Integer id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private UserEnumOptionResponse roleDetail;
    private String status;
    private UserEnumOptionResponse statusDetail;
    private List<UserEnumOptionResponse> allowedNextStatuses;
    private List<String> allowedActions;
    private List<AddressResponse> addresses;

    public UserProfileResponse(
            Integer id,
            String email,
            String fullName,
            String phoneNumber,
            String avatarUrl,
            String role,
            UserEnumOptionResponse roleDetail,
            String status,
            UserEnumOptionResponse statusDetail,
            List<UserEnumOptionResponse> allowedNextStatuses,
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
    public UserEnumOptionResponse getRoleDetail() { return roleDetail; }
    public String getStatus() { return status; }
    public UserEnumOptionResponse getStatusDetail() { return statusDetail; }
    public List<UserEnumOptionResponse> getAllowedNextStatuses() { return allowedNextStatuses; }
    public List<String> getAllowedActions() { return allowedActions; }
    public List<AddressResponse> getAddresses() { return addresses; }
}
