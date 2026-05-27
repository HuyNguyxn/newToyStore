package com.example.new_toy_store.order.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Integer id;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public OrderResponse(Integer id, String status, LocalDateTime createdAt, List<OrderItemResponse> items) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Integer getId() { return id; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItemResponse> getItems() { return items; }
}