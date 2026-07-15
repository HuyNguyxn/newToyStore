package com.example.new_toy_store.supplier_return.domain;

import com.example.new_toy_store.supplier_return.domain.exception.InvalidSupplierReturnOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum SupplierReturnReason {

    DEFECTIVE("Lỗi nhà sản xuất") {
        @Override
        public boolean isRestockable() {
            return false;
        }
    },

    EXPIRED("Cận date / Hết hạn") {
        @Override
        public boolean isRestockable() {
            return false;
        }
    },

    LIQUIDATION("Hàng bán chậm / Thanh lý") {
        @Override
        public boolean isRestockable() {
            return true;
        }
    },

    WRONG_ITEM("Giao sai hàng") {
        @Override
        public boolean isRestockable() {
            return true;
        }
    };

    private final String description;

    SupplierReturnReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract boolean isRestockable();

    @JsonCreator
    public static SupplierReturnReason from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidSupplierReturnOperationException.emptyField("Lý do trả hàng");
        }
        try {
            return SupplierReturnReason.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidSupplierReturnOperationException.invalidReason(value);
        }
    }
}