package com.example.new_toy_store.logistics.domain;

import com.example.new_toy_store.logistics.domain.exception.InvalidShipmentStatusException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ShipmentStatus {

    PENDING_PICKUP("Pending pickup", "Shipment is waiting to be handed over") {
        @Override public List<ShipmentStatus> getNextValidStates() { return List.of(IN_TRANSIT, CANCELLED); }
        @Override public List<ShipmentAction> getAvailableActions() {
            return List.of(ShipmentAction.HAND_OVER_TO_CARRIER, ShipmentAction.CANCEL_SHIPMENT);
        }
    },

    IN_TRANSIT("In transit", "Shipment is on the way to the customer") {
        @Override public List<ShipmentStatus> getNextValidStates() { return List.of(DELIVERED, DELIVERY_FAILED); }
        @Override public List<ShipmentAction> getAvailableActions() {
            return List.of(ShipmentAction.MARK_DELIVERED, ShipmentAction.REPORT_DELIVERY_FAILED);
        }
    },

    DELIVERY_FAILED("Delivery failed", "A delivery attempt failed and can be retried or returned") {
        @Override public List<ShipmentStatus> getNextValidStates() { return List.of(IN_TRANSIT, RETURNED); }
        @Override public List<ShipmentAction> getAvailableActions() {
            return List.of(ShipmentAction.RETRY_DELIVERY, ShipmentAction.RETURN_TO_WAREHOUSE);
        }
    },

    DELIVERED("Delivered", "Customer has received the shipment") {
        @Override public List<ShipmentStatus> getNextValidStates() { return List.of(); }
        @Override public List<ShipmentAction> getAvailableActions() { return List.of(); }
    },

    RETURNED("Returned", "Shipment has been returned to warehouse") {
        @Override public List<ShipmentStatus> getNextValidStates() { return List.of(); }
        @Override public List<ShipmentAction> getAvailableActions() { return List.of(); }
    },

    CANCELLED("Cancelled", "Shipment was cancelled before handover") {
        @Override public List<ShipmentStatus> getNextValidStates() { return List.of(); }
        @Override public List<ShipmentAction> getAvailableActions() { return List.of(); }
    };

    private final String displayName;
    private final String description;

    ShipmentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @JsonIgnore
    public abstract List<ShipmentStatus> getNextValidStates();

    @JsonIgnore
    public abstract List<ShipmentAction> getAvailableActions();

    public boolean canTransitionTo(ShipmentStatus targetStatus) {
        return targetStatus != null && getNextValidStates().contains(targetStatus);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ShipmentStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidShipmentStatusException.emptyStatus();
        }
        try {
            return ShipmentStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidShipmentStatusException.invalidStatus(
                    value,
                    Arrays.stream(ShipmentStatus.values()).map(Enum::name).toList()
            );
        }
    }
}
