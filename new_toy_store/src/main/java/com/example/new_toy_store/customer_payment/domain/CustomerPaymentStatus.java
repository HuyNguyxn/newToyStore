package com.example.new_toy_store.customer_payment.domain;

import com.example.new_toy_store.customer_payment.domain.exception.InvalidCustomerCustomerPaymentStatusException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CustomerPaymentStatus {

    PENDING("Pending", "Payment transaction has been created and is waiting for confirmation") {
        @Override
        public boolean canBeCancelled() { return true; }

        @Override
        public List<CustomerPaymentStatus> getNextValidStates() {
            return List.of(SUCCEEDED, FAILED, CANCELLED, EXPIRED);
        }
    },

    SUCCEEDED("Succeeded", "Payment has been completed successfully") {
        @Override
        public List<CustomerPaymentStatus> getNextValidStates() {
            return List.of(REFUND_PENDING);
        }
    },

    FAILED("Failed", "Payment was rejected or failed") {
        @Override
        public List<CustomerPaymentStatus> getNextValidStates() {
            return List.of();
        }
    },

    CANCELLED("Cancelled", "Payment was cancelled before completion") {
        @Override
        public List<CustomerPaymentStatus> getNextValidStates() {
            return List.of();
        }
    },

    EXPIRED("Expired", "Payment was not completed before the expiry time") {
        @Override
        public List<CustomerPaymentStatus> getNextValidStates() {
            return List.of();
        }
    },

    REFUND_PENDING("Refund pending", "Refund request is waiting for processing") {
        @Override
        public List<CustomerPaymentStatus> getNextValidStates() {
            return List.of(SUCCEEDED, PARTIALLY_REFUNDED, REFUNDED, REFUND_FAILED);
        }
    },

    PARTIALLY_REFUNDED("Partially refunded", "Part of the payment has been refunded") {
        @Override
        public List<CustomerPaymentStatus> getNextValidStates() {
            return List.of(REFUND_PENDING);
        }
    },

    REFUNDED("Refunded", "Payment has been refunded") {
        @Override
        public List<CustomerPaymentStatus> getNextValidStates() {
            return List.of();
        }
    },

    REFUND_FAILED("Refund failed", "Refund request failed") {
        @Override
        public List<CustomerPaymentStatus> getNextValidStates() {
            return List.of(REFUND_PENDING);
        }
    };

    private final String displayName;
    private final String description;

    CustomerPaymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean canBeCancelled() { return false; }

    @JsonIgnore
    public abstract List<CustomerPaymentStatus> getNextValidStates();

    public boolean canTransitionTo(CustomerPaymentStatus targetStatus) {
        return targetStatus != null && getNextValidStates().contains(targetStatus);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CustomerPaymentStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidCustomerCustomerPaymentStatusException.emptyStatus();
        }
        try {
            return CustomerPaymentStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidCustomerCustomerPaymentStatusException.invalidStatus(
                    value,
                    Arrays.stream(CustomerPaymentStatus.values()).map(Enum::name).toList()
            );
        }
    }
}
