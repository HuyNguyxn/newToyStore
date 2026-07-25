package com.example.new_toy_store.payment.application.dto.request;

import com.example.new_toy_store.payment.domain.PaymentMethod;
import com.example.new_toy_store.payment.domain.PaymentStatus;

import java.time.LocalDate;

public class PaymentFilterRequest {

    private Integer orderId;
    private Integer userId;
    private PaymentMethod method;
    private PaymentStatus status;
    private Double minAmount;
    private Double maxAmount;
    private LocalDate fromDate;
    private LocalDate toDate;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public Double getMinAmount() { return minAmount; }
    public void setMinAmount(Double minAmount) { this.minAmount = minAmount; }
    public Double getMaxAmount() { return maxAmount; }
    public void setMaxAmount(Double maxAmount) { this.maxAmount = maxAmount; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
}
