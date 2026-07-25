package com.example.new_toy_store.logistics.application.dto.request;

import com.example.new_toy_store.logistics.domain.ShipmentStatus;
import com.example.new_toy_store.logistics.domain.ShippingProviderCode;

import java.time.LocalDate;

public class ShipmentFilterRequest {

    private Integer orderId;
    private Integer userId;
    private ShipmentStatus status;
    private ShippingProviderCode providerCode;
    private String trackingCode;
    private LocalDate fromDate;
    private LocalDate toDate;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public ShippingProviderCode getProviderCode() { return providerCode; }
    public void setProviderCode(ShippingProviderCode providerCode) { this.providerCode = providerCode; }
    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
}
