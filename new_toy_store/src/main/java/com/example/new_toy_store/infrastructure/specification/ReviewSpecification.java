package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.review.application.dto.request.ReviewFilterRequest;
import com.example.new_toy_store.review.domain.Review;
import com.example.new_toy_store.review.domain.ReviewStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ReviewSpecification {

    private ReviewSpecification() {
    }

    public static Specification<Review> filter(ReviewFilterRequest request) {
        if (request == null) return Specification.where(null);

        return Specification.where(hasProductId(request.getProductId()))
                .and(hasRating(request.getRating()))
                .and(hasMaximumRating(request.getMaxRating()))
                .and(hasStatus(request.getStatus()))
                .and(hasAdminReplied(request.getHasAdminReplied()));
    }

    public static Specification<Review> hasProductId(Integer productId) {
        return BaseSpecification.isEqual("productId", productId);
    }

    public static Specification<Review> hasRating(Integer rating) {
        return BaseSpecification.isEqual("rating", rating);
    }

    public static Specification<Review> hasMaximumRating(Integer maxRating) {
        return (root, query, cb) -> maxRating == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("rating"), maxRating);
    }

    public static Specification<Review> hasStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return Specification.where(null);
        }
        return hasStatus(ReviewStatus.from(status));
    }

    public static Specification<Review> hasStatus(ReviewStatus status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<Review> hasAdminReplied(Boolean hasReplied) {
        return (root, query, cb) -> {
            if (hasReplied == null) {
                return cb.conjunction();
            }
            if (hasReplied) {
                return cb.and(
                        cb.isNotNull(root.get("adminReply")),
                        cb.notEqual(cb.trim(root.get("adminReply")), "")
                );
            }
            return cb.or(
                    cb.isNull(root.get("adminReply")),
                    cb.equal(cb.trim(root.get("adminReply")), "")
            );
        };
    }
}
