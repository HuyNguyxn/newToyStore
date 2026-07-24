package com.example.new_toy_store.moderation.application.dto.response;

import com.example.new_toy_store.moderation.domain.WordCategory;

import java.time.LocalDateTime;
import java.util.List;

public class BlacklistedWordResponse {
    private final Integer id;
    private final String word;
    private final WordCategory category;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
    private final List<BlacklistedWordActionResponse> availableActions;

    public BlacklistedWordResponse(Integer id,
                                   String word,
                                   WordCategory category,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt,
                                   LocalDateTime deletedAt,
                                   List<BlacklistedWordActionResponse> availableActions) {
        this.id = id;
        this.word = word;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.availableActions = availableActions;
    }

    public Integer getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public WordCategory getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public List<BlacklistedWordActionResponse> getAvailableActions() {
        return availableActions;
    }
}
