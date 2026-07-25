package com.example.new_toy_store.logistics.application.dto.response;

import com.example.new_toy_store.logistics.domain.ShipmentStatus;

import java.time.LocalDateTime;

public class ShipmentTrackingLogResponse {

    private Integer id;
    private Integer shipmentId;
    private ShipmentStatus status;
    private String location;
    private String description;
    private LocalDateTime occurredAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getShipmentId() { return shipmentId; }
    public void setShipmentId(Integer shipmentId) { this.shipmentId = shipmentId; }
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
