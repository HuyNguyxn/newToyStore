package com.example.new_toy_store.customer_return.domain;

import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReturnReasonCode {
    CHANGED_MIND("CHANGED_MIND", "Đổi ý, không muốn mua nữa") {
        @Override
        public boolean isSellable() {
            return true;
        }
    },
    DEFECTIVE("DEFECTIVE", "Hàng lỗi do nhà sản xuất") {
        @Override
        public boolean isSellable() {
            return false;
        }
    },
    WRONG_ITEM("WRONG_ITEM", "Giao sai sản phẩm") {
        @Override
        public boolean isSellable() {
            return true;
        }
    },
    DAMAGED_IN_TRANSIT("DAMAGED_IN_TRANSIT", "Hư hỏng do quá trình vận chuyển") {
        @Override
        public boolean isSellable() {
            return false;
        }
    };

    private final String code;
    private final String description;

    ReturnReasonCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public abstract boolean isSellable();

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ReturnReasonCode from(Object input) {
        if (input == null) {
            throw InvalidCustomerReturnDataException.emptyField("Lý do trả hàng");
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

    public static ReturnReasonCode from(String value) {
        return fromText(value);
    }

    private static ReturnReasonCode fromText(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            throw InvalidCustomerReturnDataException.emptyField("Lý do trả hàng");
        }
        String normalized = value.trim().toUpperCase();
        for (ReturnReasonCode reason : values()) {
            if (reason.name().equals(normalized) || reason.code.equalsIgnoreCase(value.trim())) {
                return reason;
            }
        }
        throw InvalidCustomerReturnDataException.invalidReason(value);
    }
}
