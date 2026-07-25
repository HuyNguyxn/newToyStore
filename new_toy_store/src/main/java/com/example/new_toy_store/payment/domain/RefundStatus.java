package com.example.new_toy_store.payment.domain;

import com.example.new_toy_store.payment.domain.exception.InvalidPaymentStatusException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RefundStatus {

    PENDING("Pending", "Refund request is waiting for review") {
        @Override public List<RefundStatus> getNextValidStates() { return List.of(PROCESSING, REJECTED, CANCELLED); }
    },

    PROCESSING("Processing", "Refund is being processed by staff or provider") {
        @Override public List<RefundStatus> getNextValidStates() { return List.of(SUCCEEDED, FAILED); }
    },

    SUCCEEDED("Succeeded", "Refund has been completed") {
        @Override public List<RefundStatus> getNextValidStates() { return List.of(); }
    },

    FAILED("Failed", "Refund request failed") {
        @Override public List<RefundStatus> getNextValidStates() { return List.of(PROCESSING, CANCELLED); }
    },

    REJECTED("Rejected", "Refund request was rejected") {
        @Override public List<RefundStatus> getNextValidStates() { return List.of(); }
    },

    CANCELLED("Cancelled", "Refund request was cancelled") {
        @Override public List<RefundStatus> getNextValidStates() { return List.of(); }
    };

    private final String displayName;
    private final String description;

    RefundStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @JsonIgnore
    public abstract List<RefundStatus> getNextValidStates();

    public boolean canTransitionTo(RefundStatus targetStatus) {
        return targetStatus != null && getNextValidStates().contains(targetStatus);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RefundStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidPaymentStatusException.emptyRefundStatus();
        }
        try {
            return RefundStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidPaymentStatusException.invalidRefundStatus(
                    value,
                    Arrays.stream(RefundStatus.values()).map(Enum::name).toList()
            );
        }
    }
}
