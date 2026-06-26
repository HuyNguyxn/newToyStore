package com.example.new_toy_store.review.mapper;

import com.example.new_toy_store.review.application.dto.response.ReviewResponse;
import com.example.new_toy_store.review.domain.Review;
import com.example.new_toy_store.user.domain.User;

public class ReviewMapper {
    public static ReviewResponse toResponse(Review review, User user) {
        String fullName = user != null ? user.getFullName() : "Khách hàng ẩn danh";
        String avatar = (user != null && user.getAvatarUrl() != null) ? user.getAvatarUrl() : "";

        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                fullName,
                avatar,
                review.getProductId(),
                review.getRating(),
                review.getComment(),
                review.getAdminReply(),
                review.getStatus().name(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}