package com.example.new_toy_store.payment.domain;

import com.example.new_toy_store.payment.domain.exception.InvalidPaymentStatusException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PaymentStatus {

    PENDING("Pending", "Payment transaction has been created and is waiting for confirmation") {
        @Override
        public boolean canBeCancelled() { return true; }

        @Override
        public List<PaymentStatus> getNextValidStates() {
            return List.of(SUCCEEDED, FAILED, CANCELLED, EXPIRED);
        }
    },

    SUCCEEDED("Succeeded", "Payment has been completed successfully") {
        @Override
        public List<PaymentStatus> getNextValidStates() {
            return List.of(REFUND_PENDING);
        }
    },

    FAILED("Failed", "Payment was rejected or failed") {
        @Override
        public List<PaymentStatus> getNextValidStates() {
            return List.of();
        }
    },

    CANCELLED("Cancelled", "Payment was cancelled before completion") {
        @Override
        public List<PaymentStatus> getNextValidStates() {
            return List.of();
        }
    },

    EXPIRED("Expired", "Payment was not completed before the expiry time") {
        @Override
        public List<PaymentStatus> getNextValidStates() {
            return List.of();
        }
    },

    REFUND_PENDING("Refund pending", "Refund request is waiting for processing") {
        @Override
        public List<PaymentStatus> getNextValidStates() {
            return List.of(REFUNDED, REFUND_FAILED);
        }
    },

    REFUNDED("Refunded", "Payment has been refunded") {
        @Override
        public List<PaymentStatus> getNextValidStates() {
            return List.of();
        }
    },

    REFUND_FAILED("Refund failed", "Refund request failed") {
        @Override
        public List<PaymentStatus> getNextValidStates() {
            return List.of(REFUND_PENDING);
        }
    };

    private final String displayName;
    private final String description;

    PaymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean canBeCancelled() { return false; }

    @JsonIgnore
    public abstract List<PaymentStatus> getNextValidStates();

    public boolean canTransitionTo(PaymentStatus targetStatus) {
        return targetStatus != null && getNextValidStates().contains(targetStatus);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PaymentStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidPaymentStatusException.emptyStatus();
        }
        try {
            return PaymentStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidPaymentStatusException.invalidStatus(
                    value,
                    Arrays.stream(PaymentStatus.values()).map(Enum::name).toList()
            );
        }
    }
}
