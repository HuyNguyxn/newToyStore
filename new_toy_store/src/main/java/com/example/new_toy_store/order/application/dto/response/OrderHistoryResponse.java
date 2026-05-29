package com.example.new_toy_store.order.application.dto.response;

import java.time.LocalDateTime;

public class OrderHistoryResponse {

    private String status;
    private String note;
    private LocalDateTime createdAt;

    public OrderHistoryResponse(String status, String note, LocalDateTime createdAt) {
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
    }

    public String getStatus() { return status; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}