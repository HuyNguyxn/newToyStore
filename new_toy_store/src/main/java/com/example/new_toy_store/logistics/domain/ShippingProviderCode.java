package com.example.new_toy_store.logistics.domain;

import com.example.new_toy_store.logistics.domain.exception.InvalidShipmentDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ShippingProviderCode {

    SELF_SHIPPING("Self shipping", "Internal store delivery"),
    GHN("GHN", "Giao Hang Nhanh provider reserved for a later phase");

    private final String displayName;
    private final String description;

    ShippingProviderCode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ShippingProviderCode from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidShipmentDataException("providerCode", "Shipping provider must not be empty.");
        }
        try {
            return ShippingProviderCode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidShipmentDataException("providerCode", "Shipping provider [" + value + "] is invalid.");
        }
    }
}
