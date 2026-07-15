package com.example.new_toy_store.supplier_return.application.dto.response;

import java.time.LocalDateTime;

public class SupplierReturnImageResponse {
    private Integer id;
    private String imageUrl;
    private LocalDateTime createdAt;

    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}