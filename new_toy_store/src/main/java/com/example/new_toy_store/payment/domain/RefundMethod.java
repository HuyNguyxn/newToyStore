package com.example.new_toy_store.payment.domain;

import com.example.new_toy_store.payment.domain.exception.InvalidPaymentDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RefundMethod {

    COD_MANUAL("COD manual refund", "Refund is handled manually by staff"),
    VNPAY("VNPay refund", "Refund is requested through VNPay gateway");

    private final String displayName;
    private final String description;

    RefundMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RefundMethod from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidPaymentDataException("refundMethod", "Refund method must not be empty.");
        }
        try {
            return RefundMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidPaymentDataException("refundMethod", "Refund method [" + value + "] is invalid.");
        }
    }
}
