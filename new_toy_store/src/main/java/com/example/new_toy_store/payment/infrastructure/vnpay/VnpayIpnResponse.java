package com.example.new_toy_store.payment.infrastructure.vnpay;

public class VnpayIpnResponse {

    private String RspCode;
    private String Message;

    public VnpayIpnResponse(String rspCode, String message) {
        this.RspCode = rspCode;
        this.Message = message;
    }

    public String getRspCode() { return RspCode; }
    public String getMessage() { return Message; }
}
