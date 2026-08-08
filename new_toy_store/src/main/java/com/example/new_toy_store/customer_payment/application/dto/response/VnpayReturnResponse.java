package com.example.new_toy_store.customer_payment.application.dto.response;

public class VnpayReturnResponse {

    private boolean validSignature;
    private String responseCode;
    private String transactionStatus;
    private String message;
    private CustomerPaymentResponse payment;

    public VnpayReturnResponse(
            boolean validSignature,
            String responseCode,
            String transactionStatus,
            String message,
            CustomerPaymentResponse payment
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
    public CustomerPaymentResponse getPayment() { return payment; }
}
