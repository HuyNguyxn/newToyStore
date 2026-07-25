package com.example.new_toy_store.logistics.application.dto.request;

import com.example.new_toy_store.logistics.domain.ShipmentAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ShipmentActionRequest {

    @NotNull(message = "Shipment action must not be empty")
    private ShipmentAction action;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    public ShipmentAction getAction() { return action; }
    public void setAction(ShipmentAction action) { this.action = action; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
