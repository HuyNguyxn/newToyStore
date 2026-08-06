package com.example.new_toy_store.logistics.application.dto.response;

import com.example.new_toy_store.logistics.domain.ShipmentAction;
import com.example.new_toy_store.logistics.domain.ShipmentStatus;
import com.example.new_toy_store.logistics.domain.ShipmentType;
import com.example.new_toy_store.logistics.domain.ShippingProviderCode;

import java.time.LocalDateTime;
import java.util.List;

public class ShipmentResponse {

    private Integer id;
    private String trackingCode;
    private Integer orderId;
    private Integer userId;
    private ShippingProviderCode providerCode;
    private String providerShipmentCode;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddressSnapshot;
    private double shippingFee;
    private double codAmount;
    private ShipmentStatus status;
    private ShipmentType shipmentType;
    private Integer customerReturnId;
    private Integer supplierReturnId;
    private String shipmentTypeDisplayName;
    private String providerDisplayName;
    private int deliveryAttemptCount;
    private String failureReason;
    private LocalDateTime deliveredAt;
    private LocalDateTime returnedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ShipmentItemResponse> items;
    private List<String> availableActions;
    private List<ShipmentAction> allowedActions;
    private List<ShipmentStatus> allowedNextStatuses;
    private List<ShipmentActionResponse> nextActions;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public ShippingProviderCode getProviderCode() { return providerCode; }
    public void setProviderCode(ShippingProviderCode providerCode) { this.providerCode = providerCode; }
    public String getProviderShipmentCode() { return providerShipmentCode; }
    public void setProviderShipmentCode(String providerShipmentCode) { this.providerShipmentCode = providerShipmentCode; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getShippingAddressSnapshot() { return shippingAddressSnapshot; }
    public void setShippingAddressSnapshot(String shippingAddressSnapshot) { this.shippingAddressSnapshot = shippingAddressSnapshot; }
    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }
    public double getCodAmount() { return codAmount; }
    public void setCodAmount(double codAmount) { this.codAmount = codAmount; }
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public ShipmentType getShipmentType() { return shipmentType; }
    public void setShipmentType(ShipmentType shipmentType) { this.shipmentType = shipmentType; }
    public Integer getCustomerReturnId() { return customerReturnId; }
    public void setCustomerReturnId(Integer customerReturnId) { this.customerReturnId = customerReturnId; }
    public Integer getSupplierReturnId() { return supplierReturnId; }
    public void setSupplierReturnId(Integer supplierReturnId) { this.supplierReturnId = supplierReturnId; }
    public String getShipmentTypeDisplayName() { return shipmentTypeDisplayName; }
    public void setShipmentTypeDisplayName(String shipmentTypeDisplayName) { this.shipmentTypeDisplayName = shipmentTypeDisplayName; }
    public String getProviderDisplayName() { return providerDisplayName; }
    public void setProviderDisplayName(String providerDisplayName) { this.providerDisplayName = providerDisplayName; }
    public int getDeliveryAttemptCount() { return deliveryAttemptCount; }
    public void setDeliveryAttemptCount(int deliveryAttemptCount) { this.deliveryAttemptCount = deliveryAttemptCount; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<ShipmentItemResponse> getItems() { return items; }
    public void setItems(List<ShipmentItemResponse> items) { this.items = items; }
    public List<String> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<String> availableActions) { this.availableActions = availableActions; }
    public List<ShipmentAction> getAllowedActions() { return allowedActions; }
    public void setAllowedActions(List<ShipmentAction> allowedActions) { this.allowedActions = allowedActions; }
    public List<ShipmentStatus> getAllowedNextStatuses() { return allowedNextStatuses; }
    public void setAllowedNextStatuses(List<ShipmentStatus> allowedNextStatuses) { this.allowedNextStatuses = allowedNextStatuses; }
    public List<ShipmentActionResponse> getNextActions() { return nextActions; }
    public void setNextActions(List<ShipmentActionResponse> nextActions) { this.nextActions = nextActions; }
}
