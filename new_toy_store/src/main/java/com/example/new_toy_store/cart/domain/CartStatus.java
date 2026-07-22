package com.example.new_toy_store.cart.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.new_toy_store.cart.domain.exception.InvalidCartDataException;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CartStatus {

    ACTIVE("active", "Đang mua sắm") {
        @Override
        public boolean isCheckoutInProgress() { return false; }

        @Override
        public List<CartStatus> getNextValidStates() {
            return List.of(CHECKING_OUT);
        }
    },

    CHECKING_OUT("checking_out", "Đang trong quá trình thanh toán") {
        @Override
        public boolean isCheckoutInProgress() { return true; }

        @Override
        public List<CartStatus> getNextValidStates() {
            return List.of(ACTIVE);
        }
    };

    private final String code;
    private final String description;

    CartStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }

    public String getDescription() { return description; }

    public String getName() { return this.name(); }

    public abstract boolean isCheckoutInProgress();

    public abstract List<CartStatus> getNextValidStates();

    public boolean canTransitionTo(CartStatus nextStatus) {
        return nextStatus != null && getNextValidStates().contains(nextStatus);
    }

    @JsonCreator
    public static CartStatus fromCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidCartDataException.emptyStatus();
        }

        for (CartStatus status : CartStatus.values()) {
            if (status.code.equalsIgnoreCase(value.trim()) || status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }

        throw InvalidCartDataException.invalidStatus(value);
    }
}
