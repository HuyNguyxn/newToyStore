package com.example.new_toy_store.user.application.dto.response;

import java.util.List;

public class UserProfileResponse {

    private Integer id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private String status;
    private List<AddressResponse> addresses;

    public UserProfileResponse(
            Integer id,
            String email,
            String fullName,
            String phoneNumber,
            String avatarUrl,
            String role,
            String status,
            List<AddressResponse> addresses
    ) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.status = status;
        this.addresses = addresses;
    }

    public Integer getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public List<AddressResponse> getAddresses() { return addresses; }
}
