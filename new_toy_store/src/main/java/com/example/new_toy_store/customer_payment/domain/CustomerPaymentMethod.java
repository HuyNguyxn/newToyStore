package com.example.new_toy_store.customer_payment.domain;

import com.example.new_toy_store.customer_payment.domain.exception.InvalidCustomerCustomerPaymentDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CustomerPaymentMethod {

    COD("Cash on delivery", "Customer pays when receiving the order", true),
    VNPAY("VNPay", "Online payment through VNPay gateway", true);

    private final String displayName;
    private final String description;
    private final boolean available;

    CustomerPaymentMethod(String displayName, String description, boolean available) {
        this.displayName = displayName;
        this.description = description;
        this.available = available;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean isAvailable() { return available; }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CustomerPaymentMethod from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidCustomerCustomerPaymentDataException.emptyMethod();
        }
        try {
            return CustomerPaymentMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidCustomerCustomerPaymentDataException.invalidMethod(value);
        }
    }
}
