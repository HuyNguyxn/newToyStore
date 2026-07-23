package com.example.new_toy_store.user.application.dto.response;

public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserProfileResponse user;

    public AuthResponse(String accessToken, UserProfileResponse user) {
        this(accessToken, 0L, user);
    }

    public AuthResponse(String accessToken, long expiresIn, UserProfileResponse user) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public UserProfileResponse getUser() { return user; }
}
