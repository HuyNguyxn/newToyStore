package com.example.new_toy_store.review.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponse {
    private Integer id;
    private Integer userId;
    private String userFullName;
    private String userAvatar;
    private Integer productId;
    private int rating;
    private String comment;
    private String adminReply;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> availableActions;

    public ReviewResponse(Integer id, Integer userId, String userFullName, String userAvatar, Integer productId, int rating, String comment, String adminReply, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userAvatar = userAvatar;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.adminReply = adminReply;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getUserFullName() { return userFullName; }
    public String getUserAvatar() { return userAvatar; }
    public Integer getProductId() { return productId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getAdminReply() { return adminReply; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<String> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<String> availableActions) { this.availableActions = availableActions; }
}