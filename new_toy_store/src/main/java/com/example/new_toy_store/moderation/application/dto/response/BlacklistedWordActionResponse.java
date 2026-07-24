package com.example.new_toy_store.moderation.application.dto.response;

public class BlacklistedWordActionResponse {

    private final String action;
    private final String label;

    public BlacklistedWordActionResponse(String action, String label) {
        this.action = action;
        this.label = label;
    }

    public String getAction() {
        return action;
    }

    public String getLabel() {
        return label;
    }
}
