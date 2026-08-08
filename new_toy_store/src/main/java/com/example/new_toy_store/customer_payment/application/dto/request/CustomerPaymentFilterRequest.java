package com.example.new_toy_store.customer_payment.application.dto.request;

import com.example.new_toy_store.customer_payment.domain.CustomerPaymentMethod;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentStatus;

import java.time.LocalDate;

public class CustomerPaymentFilterRequest {

    private Integer orderId;
    private Integer userId;
    private CustomerPaymentMethod method;
    private CustomerPaymentStatus status;
    private Double minAmount;
    private Double maxAmount;
    private LocalDate fromDate;
    private LocalDate toDate;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public CustomerPaymentMethod getMethod() { return method; }
    public void setMethod(CustomerPaymentMethod method) { this.method = method; }
    public CustomerPaymentStatus getStatus() { return status; }
    public void setStatus(CustomerPaymentStatus status) { this.status = status; }
    public Double getMinAmount() { return minAmount; }
    public void setMinAmount(Double minAmount) { this.minAmount = minAmount; }
    public Double getMaxAmount() { return maxAmount; }
    public void setMaxAmount(Double maxAmount) { this.maxAmount = maxAmount; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
}
