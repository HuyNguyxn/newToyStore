package com.example.new_toy_store.logistics.domain;

import com.example.new_toy_store.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shipment_tracking_logs",
        indexes = {
                @Index(name = "idx_tracking_log_shipment_occurred", columnList = "shipment_id, occurred_at"),
                @Index(name = "idx_tracking_log_status", columnList = "status")
        }
)
public class ShipmentTrackingLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "shipment_id", nullable = false)
    private Integer shipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @Column(length = 100)
    private String location;

    @Column(length = 255)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected ShipmentTrackingLog() {}

    public ShipmentTrackingLog(Integer shipmentId, ShipmentStatus status, String location, String description) {
        this.shipmentId = shipmentId;
        this.status = status;
        this.location = sanitize(location);
        this.description = sanitize(description);
        this.occurredAt = LocalDateTime.now();
    }

    private String sanitize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public Integer getId() { return id; }
    public Integer getShipmentId() { return shipmentId; }
    public ShipmentStatus getStatus() { return status; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
