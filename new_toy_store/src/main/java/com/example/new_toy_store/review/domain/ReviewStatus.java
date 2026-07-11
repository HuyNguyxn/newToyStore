package com.example.new_toy_store.review.domain;

import com.example.new_toy_store.review.domain.exception.InvalidReviewOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ReviewStatus {
    PUBLISHED("Đã hiển thị") {
        @Override
        public boolean isVisibleToPublic() {
            return true;
        }
        @Override
        public List<ReviewStatus> getNextValidStates() {
            return Collections.singletonList(HIDDEN);
        }
    },
    HIDDEN("Bị ẩn bởi Admin") {
        @Override
        public boolean isVisibleToPublic() {
            return false;
        }

        @Override
        public List<ReviewStatus> getNextValidStates() {
            return Collections.singletonList(PUBLISHED);
        }
    };

    private final String description;

    ReviewStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract boolean isVisibleToPublic();

    public abstract List<ReviewStatus> getNextValidStates();

    @JsonCreator
    public static ReviewStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidReviewOperationException.missingRequirement("status");
        }
        try {
            return ReviewStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String acceptedValues = Arrays.toString(ReviewStatus.values());
            throw InvalidReviewOperationException.invalidStatus(value, acceptedValues);
        }
    }
}