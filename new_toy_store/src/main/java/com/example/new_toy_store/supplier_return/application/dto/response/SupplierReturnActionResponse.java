package com.example.new_toy_store.supplier_return.application.dto.response;

public class SupplierReturnActionResponse {
    private final String action;
    private final String targetStatus;
    private final String label;

    public SupplierReturnActionResponse(String action, String targetStatus, String label) {
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
