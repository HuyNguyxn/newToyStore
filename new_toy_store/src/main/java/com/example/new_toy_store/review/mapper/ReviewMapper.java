package com.example.new_toy_store.review.mapper;

import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.review.application.dto.response.ReviewActionResponse;
import com.example.new_toy_store.review.application.dto.response.ReviewMediaResponse;
import com.example.new_toy_store.review.application.dto.response.ReviewResponse;
import com.example.new_toy_store.review.domain.Review;
import com.example.new_toy_store.review.domain.ReviewMedia;
import com.example.new_toy_store.review.domain.ReviewStatus;
import com.example.new_toy_store.user.domain.User;

import java.util.List;

public class ReviewMapper {

    private static final String ANONYMOUS_CUSTOMER = "Khách hàng ẩn danh";
    private static final String UNKNOWN_PRODUCT = "Sản phẩm không còn tồn tại";

    private ReviewMapper() {
    }

    public static ReviewResponse toResponse(Review review, User user, Product product) {
        return toDetailResponse(review, user, product);
    }

    public static ReviewResponse toDetailResponse(Review review, User user, Product product) {
        return createReviewResponse(
                review,
                resolveUserName(user),
                resolveUserAvatar(user),
                resolveProductName(product),
                mapAvailableActions(review.getStatus())
        );
    }

    private static ReviewResponse createReviewResponse(Review review,
                                                       String userFullName,
                                                       String userAvatar,
                                                       String productName,
                                                       List<ReviewActionResponse> availableActions) {
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                userFullName,
                userAvatar,
                review.getProductId(),
                productName,
                review.getOrderItemId(),
                review.getVariantAttributesSnapshot(),
                review.getRating(),
                review.getComment(),
                mapMediaAttachments(review),
                review.getAdminReply(),
                review.getStatus(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                availableActions
        );
    }

    private static List<ReviewActionResponse> mapAvailableActions(ReviewStatus status) {
        if (status == null) {
            return List.of();
        }

        return status.getAllowedNextStatusCodes().stream()
                .map(ReviewMapper::mapStatusAction)
                .toList();
    }

    private static List<ReviewMediaResponse> mapMediaAttachments(Review review) {
        if (review.getMediaAttachments() == null || review.getMediaAttachments().isEmpty()) {
            return List.of();
        }

        return review.getMediaAttachments().stream()
                .map(ReviewMapper::mapMediaAttachment)
                .toList();
    }

    private static ReviewMediaResponse mapMediaAttachment(ReviewMedia media) {
        return new ReviewMediaResponse(
                media.getId(),
                media.getMediaType(),
                media.getMediaUrl(),
                media.getDisplayOrder()
        );
    }

    private static ReviewActionResponse mapStatusAction(String targetStatus) {
        if (ReviewStatus.PUBLISHED.getCode().equals(targetStatus)) {
            return new ReviewActionResponse("PUBLISH", targetStatus, "Hiển thị đánh giá");
        }
        if (ReviewStatus.HIDDEN.getCode().equals(targetStatus)) {
            return new ReviewActionResponse("HIDE", targetStatus, "Ẩn đánh giá");
        }
        return new ReviewActionResponse("CHANGE_STATUS", targetStatus, "Chuyển trạng thái");
    }

    private static String resolveUserName(User user) {
        return user != null ? user.getFullName() : ANONYMOUS_CUSTOMER;
    }

    private static String resolveUserAvatar(User user) {
        return user != null && user.getAvatarUrl() != null ? user.getAvatarUrl() : "";
    }

    private static String resolveProductName(Product product) {
        return product != null ? product.getName() : UNKNOWN_PRODUCT;
    }
}
