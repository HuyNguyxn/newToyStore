package com.example.new_toy_store.order.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Integer id;
    private Integer userId;
    private String status;
    private double totalAmount;
    private String shippingAddress;
    private String promoCode;
    private double discountAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
    private List<OrderHistoryResponse> histories;

    public OrderResponse(Integer id, Integer userId, String status, double totalAmount,
                         String shippingAddress, String promoCode, double discountAmount,
                         LocalDateTime createdAt, LocalDateTime updatedAt,
                         List<OrderItemResponse> items, List<OrderHistoryResponse> histories) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.promoCode = promoCode;
        this.discountAmount = discountAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
        this.histories = histories;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPromoCode() { return promoCode; }
    public double getDiscountAmount() { return discountAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderItemResponse> getItems() { return items; }
    public List<OrderHistoryResponse> getHistories() { return histories; }
}