package com.example.new_toy_store.customer_return.application.dto.response;

public class CustomerReturnActionResponse {
    private final String action;
    private final String targetStatus;
    private final String label;

    public CustomerReturnActionResponse(String action, String targetStatus, String label) {
        this.action = action;
        this.targetStatus = targetStatus;
        this.label = label;
    }

    public String getAction() {
        return action;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public String getLabel() {
        return label;
    }
}
