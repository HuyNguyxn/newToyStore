package com.example.new_toy_store.supplier_payment.domain;

import com.example.new_toy_store.supplier_payment.domain.exception.InvalidSupplierPaymentOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SupplierPaymentMethod {
    CASH("Tiền mặt"),
    BANK_TRANSFER("Chuyển khoản"),
    OTHER("Khác");

    private final String displayName;

    SupplierPaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    @JsonProperty("code")
    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SupplierPaymentMethod from(Object rawValue) {
        if (rawValue == null) {
            throw InvalidSupplierPaymentOperationException.emptyField("Phương thức thanh toán");
        }

        String value = rawValue instanceof String stringValue
                ? stringValue
                : String.valueOf(rawValue);

        if (value.isBlank()) {
            throw InvalidSupplierPaymentOperationException.emptyField("Phương thức thanh toán");
        }

        try {
            return SupplierPaymentMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidSupplierPaymentOperationException.invalidMethod(value);
        }
    }
}
