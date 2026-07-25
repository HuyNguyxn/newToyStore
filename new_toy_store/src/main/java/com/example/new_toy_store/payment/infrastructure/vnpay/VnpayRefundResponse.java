package com.example.new_toy_store.payment.infrastructure.vnpay;

public class VnpayRefundResponse {

    private final boolean success;
    private final String providerRefundId;
    private final String responseCode;
    private final String message;

    public VnpayRefundResponse(boolean success, String providerRefundId, String responseCode, String message) {
        this.success = success;
        this.providerRefundId = providerRefundId;
        this.responseCode = responseCode;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getProviderRefundId() { return providerRefundId; }
    public String getResponseCode() { return responseCode; }
    public String getMessage() { return message; }
}
