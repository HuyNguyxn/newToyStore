package com.example.new_toy_store.moderation.application.dto.response;

import java.time.LocalDateTime;

public class BlacklistedWordResponse {
    private Integer id;
    private String word;
    private String category;
    private String categoryDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public BlacklistedWordResponse(Integer id, String word, String category, String categoryDescription, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.word = word;
        this.category = category;
        this.categoryDescription = categoryDescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public Integer getId() { return id; }
    public String getWord() { return word; }
    public String getCategory() { return category; }
    public String getCategoryDescription() { return categoryDescription; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}