package com.example.new_toy_store.review.mapper;

import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.review.application.dto.response.ReviewResponse;
import com.example.new_toy_store.review.domain.Review;
import com.example.new_toy_store.user.domain.User;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewMapper {
    public static ReviewResponse toResponse(Review review, User user, Product product) {
        String fullName = user != null ? user.getFullName() : "Khách hàng ẩn danh";
        String avatar = (user != null && user.getAvatarUrl() != null) ? user.getAvatarUrl() : "";
        String productName = product != null ? product.getName() : "Sản phẩm không còn tồn tại";

        ReviewResponse response = new ReviewResponse(
                review.getId(),
                review.getUserId(),
                fullName,
                avatar,
                review.getProductId(),
                productName,
                review.getOrderItemId(),
                review.getVariantAttributesSnapshot(),
                review.getRating(),
                review.getComment(),
                review.getAdminReply(),
                review.getStatus().name(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );

        if (review.getStatus() != null) {
            List<String> actions = review.getStatus().getNextValidStates().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            response.setAvailableActions(actions);
        }

        return response;
    }
}