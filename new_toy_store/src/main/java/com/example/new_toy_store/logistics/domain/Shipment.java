package com.example.new_toy_store.logistics.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.logistics.domain.exception.InvalidShipmentDataException;
import com.example.new_toy_store.logistics.domain.exception.InvalidShipmentOperationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "shipments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shipment_tracking_code", columnNames = "tracking_code"),
                @UniqueConstraint(name = "uk_shipment_order", columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_shipment_order", columnList = "order_id"),
                @Index(name = "idx_shipment_user", columnList = "user_id"),
                @Index(name = "idx_shipment_status", columnList = "status"),
                @Index(name = "idx_shipment_provider", columnList = "provider_code"),
                @Index(name = "idx_shipment_created_at", columnList = "created_at"),
                @Index(name = "idx_shipment_status_created", columnList = "status, created_at")
        }
)
public class Shipment extends BaseRootEntity {

    public static final int DEFAULT_MAX_DELIVERY_ATTEMPTS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tracking_code", nullable = false, length = 40)
    private String trackingCode;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_code", nullable = false, length = 30)
    private ShippingProviderCode providerCode;

    @Column(name = "provider_shipment_code", length = 80)
    private String providerShipmentCode;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 30)
    private String recipientPhone;

    @Column(name = "shipping_address_snapshot", nullable = false, length = 500)
    private String shippingAddressSnapshot;

    @Column(name = "shipping_fee", nullable = false)
    private double shippingFee = 0.0;

    @Column(name = "cod_amount", nullable = false)
    private double codAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @Column(name = "delivery_attempt_count", nullable = false)
    private int deliveryAttemptCount = 0;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> items = new ArrayList<>();

    protected Shipment() {}

    public Shipment(
            String trackingCode,
            Integer orderId,
            Integer userId,
            String recipientName,
            String recipientPhone,
            String shippingAddressSnapshot,
            double shippingFee,
            double codAmount
    ) {
        if (trackingCode == null || trackingCode.trim().isEmpty()) {
            throw new InvalidShipmentDataException("trackingCode", "Tracking code must not be empty.");
        }
        if (orderId == null) throw new InvalidShipmentDataException("orderId", "Order id must not be empty.");
        if (userId == null) throw new InvalidShipmentDataException("userId", "User id must not be empty.");
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new InvalidShipmentDataException("recipientName", "Recipient name must not be empty.");
        }
        if (shippingAddressSnapshot == null || shippingAddressSnapshot.trim().isEmpty()) {
            throw new InvalidShipmentDataException("shippingAddressSnapshot", "Shipping address must not be empty.");
        }

        this.trackingCode = trackingCode.trim();
        this.orderId = orderId;
        this.userId = userId;
        this.providerCode = ShippingProviderCode.SELF_SHIPPING;
        this.recipientName = recipientName.trim();
        this.recipientPhone = sanitize(recipientPhone);
        this.shippingAddressSnapshot = shippingAddressSnapshot.trim();
        this.shippingFee = roundAmount(shippingFee);
        this.codAmount = roundAmount(codAmount);
        this.status = ShipmentStatus.PENDING_PICKUP;
    }

    public void addItem(Integer productId, Integer variantId, String productNameSnapshot, String variantSnapshot, int quantity) {
        ShipmentItem item = new ShipmentItem(productId, variantId, productNameSnapshot, variantSnapshot, quantity);
        item.setShipment(this);
        this.items.add(item);
    }

    public void handOverToCarrier(String location, String note) {
        transitionTo(ShipmentStatus.IN_TRANSIT);
        this.failureReason = null;
    }

    public void markDelivered(String location, String note) {
        transitionTo(ShipmentStatus.DELIVERED);
        this.deliveredAt = LocalDateTime.now();
        this.failureReason = null;
    }

    public void reportDeliveryFailed(String reason) {
        if (this.deliveryAttemptCount >= DEFAULT_MAX_DELIVERY_ATTEMPTS) {
            throw new InvalidShipmentOperationException("reportDeliveryFailed", "Maximum delivery attempts reached.");
        }
        transitionTo(ShipmentStatus.DELIVERY_FAILED);
        this.deliveryAttemptCount++;
        this.failureReason = requireReason(reason);
    }

    public void retryDelivery(String location, String note) {
        transitionTo(ShipmentStatus.IN_TRANSIT);
    }

    public void returnToWarehouse(String reason) {
        transitionTo(ShipmentStatus.RETURNED);
        this.returnedAt = LocalDateTime.now();
        this.failureReason = requireReason(reason);
    }

    public void cancel(String reason) {
        transitionTo(ShipmentStatus.CANCELLED);
        this.cancelledAt = LocalDateTime.now();
        this.failureReason = requireReason(reason);
    }

    private void transitionTo(ShipmentStatus targetStatus) {
        if (!this.status.canTransitionTo(targetStatus)) {
            throw InvalidShipmentOperationException.invalidTransition(this.id, this.status, targetStatus);
        }
        this.status = targetStatus;
    }

    private String requireReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidShipmentDataException("reason", "Reason must not be empty.");
        }
        return reason.trim();
    }

    private String sanitize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private double roundAmount(double value) {
        double limitedValue = Math.min(value, 999_999_999.99);
        return Math.max(0.0, Math.round(limitedValue * 100.0) / 100.0);
    }

    public Integer getId() { return id; }
    public String getTrackingCode() { return trackingCode; }
    public Integer getOrderId() { return orderId; }
    public Integer getUserId() { return userId; }
    public ShippingProviderCode getProviderCode() { return providerCode; }
    public String getProviderShipmentCode() { return providerShipmentCode; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public String getShippingAddressSnapshot() { return shippingAddressSnapshot; }
    public double getShippingFee() { return shippingFee; }
    public double getCodAmount() { return codAmount; }
    public ShipmentStatus getStatus() { return status; }
    public int getDeliveryAttemptCount() { return deliveryAttemptCount; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public List<ShipmentItem> getItems() { return Collections.unmodifiableList(items); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Shipment other && id != null && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
