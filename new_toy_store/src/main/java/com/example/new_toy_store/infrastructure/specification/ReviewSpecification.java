package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.review.application.dto.request.ReviewFilterRequest;
import com.example.new_toy_store.review.domain.Review;
import com.example.new_toy_store.review.domain.ReviewStatus;
import org.springframework.data.jpa.domain.Specification;

public class ReviewSpecification {

    public static Specification<Review> filter(ReviewFilterRequest request) {
        if (request == null) return Specification.where(null);

        Specification<Review> spec = Specification.where(BaseSpecification.<Review>isEqual("productId", request.getProductId()))
                .and(BaseSpecification.isEqual("rating", request.getRating()));

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            ReviewStatus statusEnum = ReviewStatus.valueOf(request.getStatus().trim().toUpperCase());
            spec = spec.and(BaseSpecification.isEqual("status", statusEnum));
        }

        if (request.getHasAdminReplied() != null) {
            spec = spec.and(hasAdminReplied(request.getHasAdminReplied()));
        }

        return spec;
    }

    private static Specification<Review> hasAdminReplied(boolean hasReplied) {
        return (root, query, cb)
                -> hasReplied ? cb.isNotNull(root.get("adminReply")) : cb.isNull(root.get("adminReply"));
    }
}