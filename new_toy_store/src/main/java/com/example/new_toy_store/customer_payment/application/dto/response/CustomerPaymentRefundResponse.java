package com.example.new_toy_store.customer_payment.application.dto.response;

import com.example.new_toy_store.customer_payment.domain.RefundMethod;
import com.example.new_toy_store.customer_payment.domain.RefundStatus;

import java.time.LocalDateTime;
import java.util.List;

public class CustomerPaymentRefundResponse {

    private Integer id;
    private Integer paymentId;
    private Integer orderId;
    private Integer userId;
    private String refundCode;
    private RefundMethod method;
    private RefundStatus status;
    private double amount;
    private String reason;
    private String providerRefundId;
    private String failedReason;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RefundStatus> allowedNextStatuses;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getRefundCode() { return refundCode; }
    public void setRefundCode(String refundCode) { this.refundCode = refundCode; }
    public RefundMethod getMethod() { return method; }
    public void setMethod(RefundMethod method) { this.method = method; }
    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getProviderRefundId() { return providerRefundId; }
    public void setProviderRefundId(String providerRefundId) { this.providerRefundId = providerRefundId; }
    public String getFailedReason() { return failedReason; }
    public void setFailedReason(String failedReason) { this.failedReason = failedReason; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<RefundStatus> getAllowedNextStatuses() { return allowedNextStatuses; }
    public void setAllowedNextStatuses(List<RefundStatus> allowedNextStatuses) { this.allowedNextStatuses = allowedNextStatuses; }
}
