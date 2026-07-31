package com.example.new_toy_store.review.application.dto.response;

import com.example.new_toy_store.review.domain.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponse {
    private Integer id;
    private Integer userId;
    private String userFullName;
    private String userAvatar;

    private Integer productId;
    private String productName;
    private Integer orderItemId;
    private String variantAttributesSnapshot;

    private int rating;
    private String comment;
    private List<ReviewMediaResponse> mediaAttachments;
    private String adminReply;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ReviewActionResponse> availableActions;

    public ReviewResponse(Integer id,
                          Integer userId,
                          String userFullName,
                          String userAvatar,
                          Integer productId,
                          String productName,
                          Integer orderItemId,
                          String variantAttributesSnapshot,
                          int rating,
                          String comment,
                          List<ReviewMediaResponse> mediaAttachments,
                          String adminReply,
                          ReviewStatus status,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt,
                          List<ReviewActionResponse> availableActions) {
        this.id = id;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userAvatar = userAvatar;
        this.productId = productId;
        this.productName = productName;
        this.orderItemId = orderItemId;
        this.variantAttributesSnapshot = variantAttributesSnapshot;
        this.rating = rating;
        this.comment = comment;
        this.mediaAttachments = mediaAttachments;
        this.adminReply = adminReply;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.availableActions = availableActions;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getUserFullName() { return userFullName; }
    public String getUserAvatar() { return userAvatar; }
    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getOrderItemId() { return orderItemId; }
    public String getVariantAttributesSnapshot() { return variantAttributesSnapshot; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public List<ReviewMediaResponse> getMediaAttachments() { return mediaAttachments; }
    public String getAdminReply() { return adminReply; }
    public ReviewStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<ReviewActionResponse> getAvailableActions() { return availableActions; }
}
