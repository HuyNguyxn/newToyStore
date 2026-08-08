package com.example.new_toy_store.customer_payment.application.dto.request;

import com.example.new_toy_store.customer_payment.domain.RefundMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CustomerPaymentRefundRequest {

    @NotNull(message = "Refund amount must not be empty")
    @Positive(message = "Refund amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Refund method must not be empty")
    private RefundMethod method;

    @NotBlank(message = "Refund reason must not be empty")
    @Size(max = 255, message = "Refund reason must not exceed 255 characters")
    private String reason;

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public RefundMethod getMethod() { return method; }
    public void setMethod(RefundMethod method) { this.method = method; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
