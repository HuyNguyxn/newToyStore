package com.example.new_toy_store.promotion.application.dto.response;

public class PromotionActionResponse {

    private final String action;
    private final String label;

    public PromotionActionResponse(String action, String label) {
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
