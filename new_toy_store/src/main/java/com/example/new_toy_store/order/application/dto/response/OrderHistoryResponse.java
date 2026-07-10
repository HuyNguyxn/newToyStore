package com.example.new_toy_store.order.application.dto.response;

import com.example.new_toy_store.order.domain.OrderStatus;
import java.time.LocalDateTime;

public class OrderHistoryResponse {

    private Integer id;
    private OrderStatus status;
    private String note;
    private LocalDateTime createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}