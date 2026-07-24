package com.example.new_toy_store.order.application.dto.response;

import com.example.new_toy_store.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Integer id;
    private Integer userId;
    private OrderStatus status;
    private double totalAmount;
    private String shippingAddress;
    private String promoCode;
    private double discountAmount;
    private List<OrderItemResponse> items;
    private List<OrderHistoryResponse> histories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> availableActions;
    private List<OrderStatus> allowedNextActions;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    public List<OrderHistoryResponse> getHistories() { return histories; }
    public void setHistories(List<OrderHistoryResponse> histories) { this.histories = histories; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<String> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<String> availableActions) { this.availableActions = availableActions; }
    public List<OrderStatus> getAllowedNextActions() { return allowedNextActions; }
    public void setAllowedNextActions(List<OrderStatus> allowedNextActions) { this.allowedNextActions = allowedNextActions; }
}
