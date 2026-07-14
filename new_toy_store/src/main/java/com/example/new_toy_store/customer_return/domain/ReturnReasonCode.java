package com.example.new_toy_store.customer_return.domain;

import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnDataException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum ReturnReasonCode {
    CHANGED_MIND("Đổi ý, không muốn mua nữa") {
        @Override public boolean isSellable() { return true; }
    },
    DEFECTIVE("Hàng lỗi do nhà sản xuất") {
        @Override public boolean isSellable() { return false; }
    },
    WRONG_ITEM("Giao sai sản phẩm") {
        @Override public boolean isSellable() { return true; }
    },
    DAMAGED_IN_TRANSIT("Hư hỏng do quá trình vận chuyển") {
        @Override public boolean isSellable() { return false; }
    };

    private final String description;

    ReturnReasonCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract boolean isSellable();

    @JsonCreator
    public static ReturnReasonCode from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidCustomerReturnDataException.emptyField("Lý do trả hàng");
        }
        try {
            return ReturnReasonCode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidCustomerReturnDataException.invalidReason(value);
        }
    }
}