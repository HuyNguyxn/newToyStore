package com.example.new_toy_store.notification.application.dto.request;

import jakarta.validation.constraints.NotNull;

public class NotificationPreferenceRequest {

    @NotNull private Boolean inAppEnabled;
    @NotNull private Boolean emailEnabled;
    @NotNull private Boolean orderEnabled;
    @NotNull private Boolean paymentEnabled;
    @NotNull private Boolean shipmentEnabled;
    @NotNull private Boolean returnEnabled;
    @NotNull private Boolean reviewEnabled;
    @NotNull private Boolean cartEnabled;
    @NotNull private Boolean systemEnabled;

    public Boolean getInAppEnabled() { return inAppEnabled; }
    public void setInAppEnabled(Boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }
    public Boolean getEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(Boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    public Boolean getOrderEnabled() { return orderEnabled; }
    public void setOrderEnabled(Boolean orderEnabled) { this.orderEnabled = orderEnabled; }
    public Boolean getPaymentEnabled() { return paymentEnabled; }
    public void setPaymentEnabled(Boolean paymentEnabled) { this.paymentEnabled = paymentEnabled; }
    public Boolean getShipmentEnabled() { return shipmentEnabled; }
    public void setShipmentEnabled(Boolean shipmentEnabled) { this.shipmentEnabled = shipmentEnabled; }
    public Boolean getReturnEnabled() { return returnEnabled; }
    public void setReturnEnabled(Boolean returnEnabled) { this.returnEnabled = returnEnabled; }
    public Boolean getReviewEnabled() { return reviewEnabled; }
    public void setReviewEnabled(Boolean reviewEnabled) { this.reviewEnabled = reviewEnabled; }
    public Boolean getCartEnabled() { return cartEnabled; }
    public void setCartEnabled(Boolean cartEnabled) { this.cartEnabled = cartEnabled; }
    public Boolean getSystemEnabled() { return systemEnabled; }
    public void setSystemEnabled(Boolean systemEnabled) { this.systemEnabled = systemEnabled; }
}
