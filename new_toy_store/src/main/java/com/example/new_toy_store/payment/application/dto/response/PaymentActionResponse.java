package com.example.new_toy_store.payment.application.dto.response;

public class PaymentActionResponse {

    private String code;
    private String label;
    private String description;

    public PaymentActionResponse(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
}
