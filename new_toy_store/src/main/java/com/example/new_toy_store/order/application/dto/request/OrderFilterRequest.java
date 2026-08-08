package com.example.new_toy_store.order.application.dto.request;

import com.example.new_toy_store.order.domain.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;

public class OrderFilterRequest {
    private Integer userId;
    private OrderStatus status;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    @Min(value = 0, message = "Số tiền lọc tối thiểu không được âm")
    private Double minAmount;

    @Min(value = 0, message = "Số tiền lọc tối đa không được âm")
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

    @AssertTrue(message = "Khoảng thời gian không hợp lệ")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !fromDate.isAfter(toDate);
    }

    @AssertTrue(message = "Khoảng số tiền không hợp lệ")
    public boolean isAmountRangeValid() {
        return minAmount == null || maxAmount == null || minAmount <= maxAmount;
    }
}
