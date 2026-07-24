package com.example.new_toy_store.review.domain.exception;

import java.util.Map;

public class ReviewNotFoundException extends RuntimeException {
    private final Integer reviewId;

    public ReviewNotFoundException(Integer reviewId) {
        super(String.format("Không tìm thấy dữ liệu đánh giá với mã: %d", reviewId));
        this.reviewId = reviewId;
    }

    public Map<String, ?> getContextData() {
        return Map.of("reviewId", reviewId);
    }

    public Integer getReviewId() {
        return reviewId;
    }
}
