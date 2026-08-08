package com.example.new_toy_store.customer_payment.application.dto.response;

public class CustomerPaymentActionResponse {

    private String code;
    private String label;
    private String description;

    public CustomerPaymentActionResponse(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
}
