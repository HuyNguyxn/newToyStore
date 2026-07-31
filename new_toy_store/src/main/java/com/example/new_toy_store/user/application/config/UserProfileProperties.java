package com.example.new_toy_store.user.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.user")
public class UserProfileProperties {

    private String defaultAvatarUrl = "";

    public String getDefaultAvatarUrl() {
        return defaultAvatarUrl;
    }

    public void setDefaultAvatarUrl(String defaultAvatarUrl) {
        this.defaultAvatarUrl = defaultAvatarUrl;
    }
}
