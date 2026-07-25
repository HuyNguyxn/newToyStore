package com.example.new_toy_store.payment.application.dto.request;

import jakarta.validation.constraints.Size;

public class PaymentConfirmRequest {

    @Size(max = 100, message = "Provider transaction id must not exceed 100 characters")
    private String providerTransactionId;

    public String getProviderTransactionId() { return providerTransactionId; }
    public void setProviderTransactionId(String providerTransactionId) { this.providerTransactionId = providerTransactionId; }
}
