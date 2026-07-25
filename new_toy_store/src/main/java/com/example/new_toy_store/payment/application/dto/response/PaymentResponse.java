package com.example.new_toy_store.payment.application.dto.response;

import com.example.new_toy_store.payment.domain.PaymentMethod;
import com.example.new_toy_store.payment.domain.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentResponse {

    private Integer id;
    private Integer orderId;
    private Integer userId;
    private PaymentMethod method;
    private PaymentStatus status;
    private double amount;
    private String providerTransactionId;
    private String failureReason;
    private String cancelReason;
    private LocalDateTime paidAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String paymentUrl;
    private String gatewayMessage;
    private List<String> availableActions;
    private List<PaymentStatus> allowedNextStatuses;
    private List<PaymentActionResponse> nextActions;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getProviderTransactionId() { return providerTransactionId; }
    public void setProviderTransactionId(String providerTransactionId) { this.providerTransactionId = providerTransactionId; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
    public String getGatewayMessage() { return gatewayMessage; }
    public void setGatewayMessage(String gatewayMessage) { this.gatewayMessage = gatewayMessage; }
    public List<String> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<String> availableActions) { this.availableActions = availableActions; }
    public List<PaymentStatus> getAllowedNextStatuses() { return allowedNextStatuses; }
    public void setAllowedNextStatuses(List<PaymentStatus> allowedNextStatuses) { this.allowedNextStatuses = allowedNextStatuses; }
    public List<PaymentActionResponse> getNextActions() { return nextActions; }
    public void setNextActions(List<PaymentActionResponse> nextActions) { this.nextActions = nextActions; }
}
