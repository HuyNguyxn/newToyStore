package com.example.new_toy_store.customer_payment.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerPaymentRefundDecisionRequest {

    @NotBlank(message = "Reason must not be empty")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
