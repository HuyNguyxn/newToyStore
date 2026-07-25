package com.example.new_toy_store.payment.application.dto.response;

public class VnpayReturnResponse {

    private boolean validSignature;
    private String responseCode;
    private String transactionStatus;
    private String message;
    private PaymentResponse payment;

    public VnpayReturnResponse(
            boolean validSignature,
            String responseCode,
            String transactionStatus,
            String message,
            PaymentResponse payment
    ) {
        this.validSignature = validSignature;
        this.responseCode = responseCode;
        this.transactionStatus = transactionStatus;
        this.message = message;
        this.payment = payment;
    }

    public boolean isValidSignature() { return validSignature; }
    public String getResponseCode() { return responseCode; }
    public String getTransactionStatus() { return transactionStatus; }
    public String getMessage() { return message; }
    public PaymentResponse getPayment() { return payment; }
}
