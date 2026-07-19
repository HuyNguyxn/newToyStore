package com.example.new_toy_store.cart.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum CartStatus {

    ACTIVE("active") {
        @Override
        public boolean canTransitionTo(CartStatus nextStatus) {
            return nextStatus == CHECKING_OUT || nextStatus == ACTIVE;
        }
    },
    CHECKING_OUT("checking_out") {
        @Override
        public boolean canTransitionTo(CartStatus nextStatus) {
            return nextStatus == ACTIVE || nextStatus == CHECKING_OUT;
        }
    };

    private final String code;

    CartStatus(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public abstract boolean canTransitionTo(CartStatus nextStatus);

    @JsonCreator
    public static CartStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi rác dữ liệu: Trạng thái giỏ hàng không được để trống");
        }

        return Arrays.stream(CartStatus.values())
                .filter(status -> status.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lỗi rác dữ liệu: Không tồn tại trạng thái giỏ hàng mang mã '" + code + "'"));
    }
}