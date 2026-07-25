package com.example.new_toy_store.logistics.domain;

import com.example.new_toy_store.logistics.domain.exception.InvalidShipmentDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ShipmentAction {

    HAND_OVER_TO_CARRIER("Hand over to carrier", "Move shipment to in transit"),
    MARK_DELIVERED("Mark delivered", "Mark shipment as delivered"),
    REPORT_DELIVERY_FAILED("Report delivery failed", "Record one failed delivery attempt"),
    RETRY_DELIVERY("Retry delivery", "Move shipment back to in transit"),
    RETURN_TO_WAREHOUSE("Return to warehouse", "Mark shipment as returned to warehouse"),
    CANCEL_SHIPMENT("Cancel shipment", "Cancel shipment before handover");

    private final String displayName;
    private final String description;

    ShipmentAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ShipmentAction from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidShipmentDataException("action", "Shipment action must not be empty.");
        }
        try {
            return ShipmentAction.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidShipmentDataException("action", "Shipment action [" + value + "] is invalid.");
        }
    }
}
