package com.example.new_toy_store.supplier_return.domain;

import com.example.new_toy_store.supplier_return.domain.exception.InvalidSupplierReturnOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SupplierReturnReason {

    DEFECTIVE("DEFECTIVE", "Lỗi nhà sản xuất") {
        @Override
        public boolean isRestockable() {
            return false;
        }
    },

    EXPIRED("EXPIRED", "Cận date / Hết hạn") {
        @Override
        public boolean isRestockable() {
            return false;
        }
    },

    LIQUIDATION("LIQUIDATION", "Hàng bán chậm / Thanh lý") {
        @Override
        public boolean isRestockable() {
            return true;
        }
    },

    WRONG_ITEM("WRONG_ITEM", "Giao sai hàng") {
        @Override
        public boolean isRestockable() {
            return true;
        }
    };

    private final String code;
    private final String description;

    SupplierReturnReason(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public abstract boolean isRestockable();

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SupplierReturnReason from(Object input) {
        if (input == null) {
            throw InvalidSupplierReturnOperationException.emptyField("Lý do trả hàng");
        }

        if (input instanceof Map<?, ?> objectValue) {
            Object codeValue = objectValue.get("code");
            if (codeValue == null) {
                codeValue = objectValue.get("name");
            }
            if (codeValue == null) {
                codeValue = objectValue.get("reasonCode");
            }
            return fromText(String.valueOf(codeValue));
        }

        return fromText(String.valueOf(input));
    }

    public static SupplierReturnReason from(String value) {
        return fromText(value);
    }

    private static SupplierReturnReason fromText(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            throw InvalidSupplierReturnOperationException.emptyField("Lý do trả hàng");
        }
        String normalized = value.trim().toUpperCase();
        for (SupplierReturnReason reason : values()) {
            if (reason.name().equals(normalized) || reason.code.equalsIgnoreCase(value.trim())) {
                return reason;
            }
        }
        throw InvalidSupplierReturnOperationException.invalidReason(value);
    }
}
