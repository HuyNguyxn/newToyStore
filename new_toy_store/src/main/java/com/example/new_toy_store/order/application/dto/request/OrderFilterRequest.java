package com.example.new_toy_store.order.application.dto.request;

import com.example.new_toy_store.order.domain.OrderStatus;
import java.time.LocalDateTime;

public class OrderFilterRequest {
    private Integer userId;
    private OrderStatus status;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Double minAmount;
    private Double maxAmount;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getFromDate() { return fromDate; }
    public void setFromDate(LocalDateTime fromDate) { this.fromDate = fromDate; }
    public LocalDateTime getToDate() { return toDate; }
    public void setToDate(LocalDateTime toDate) { this.toDate = toDate; }
    public Double getMinAmount() { return minAmount; }
    public void setMinAmount(Double minAmount) { this.minAmount = minAmount; }
    public Double getMaxAmount() { return maxAmount; }
    public void setMaxAmount(Double maxAmount) { this.maxAmount = maxAmount; }
}