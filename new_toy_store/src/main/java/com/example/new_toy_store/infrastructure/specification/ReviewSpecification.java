package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.review.application.dto.request.ReviewFilterRequest;
import com.example.new_toy_store.review.domain.Review;
import com.example.new_toy_store.review.domain.ReviewStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ReviewSpecification {

    public static Specification<Review> filter(ReviewFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getProductId() != null) {
                predicates.add(cb.equal(root.get("productId"), request.getProductId()));
            }

            if (request.getRating() != null) {
                predicates.add(cb.equal(root.get("rating"), request.getRating()));
            }

            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                try {
                    ReviewStatus statusEnum = ReviewStatus.valueOf(request.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                }
            }

            if (request.getHasAdminReplied() != null) {
                if (request.getHasAdminReplied()) {
                    predicates.add(cb.isNotNull(root.get("adminReply")));
                } else {
                    predicates.add(cb.isNull(root.get("adminReply")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}