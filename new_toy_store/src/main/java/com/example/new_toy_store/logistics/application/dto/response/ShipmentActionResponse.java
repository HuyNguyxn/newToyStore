package com.example.new_toy_store.logistics.application.dto.response;

public class ShipmentActionResponse {

    private String code;
    private String label;
    private String description;

    public ShipmentActionResponse(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
}
