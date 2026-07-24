package com.example.new_toy_store.review.domain;

import com.example.new_toy_store.review.domain.exception.InvalidReviewOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReviewStatus {
    PUBLISHED(
            "PUBLISHED",
            "Đã hiển thị",
            "Đánh giá đang được hiển thị công khai."
    ) {
        @Override
        public boolean isVisibleToPublic() {
            return true;
        }

        @Override
        protected Set<ReviewStatus> nextStatuses() {
            return EnumSet.of(HIDDEN);
        }
    },
    HIDDEN(
            "HIDDEN",
            "Bị ẩn bởi Admin",
            "Đánh giá đã bị ẩn khỏi khu vực công khai."
    ) {
        @Override
        public boolean isVisibleToPublic() {
            return false;
        }

        @Override
        protected Set<ReviewStatus> nextStatuses() {
            return EnumSet.of(PUBLISHED);
        }
    };

    private final String code;
    private final String displayName;
    private final String description;

    ReviewStatus(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    protected abstract Set<ReviewStatus> nextStatuses();

    public abstract boolean isVisibleToPublic();

    public boolean canTransitionTo(ReviewStatus nextStatus) {
        return nextStatus != null && nextStatuses().contains(nextStatus);
    }

    @JsonIgnore
    public List<ReviewStatus> getNextValidStates() {
        return nextStatuses().stream().toList();
    }

    public List<String> getAllowedNextStatusCodes() {
        return nextStatuses().stream()
                .map(ReviewStatus::getCode)
                .toList();
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ReviewStatus from(Object input) {
        if (input == null) {
            throw InvalidReviewOperationException.missingRequirement("status");
        }

        if (input instanceof Map<?, ?> objectValue) {
            Object codeValue = objectValue.get("code");
            if (codeValue == null) {
                codeValue = objectValue.get("name");
            }
            if (codeValue == null) {
                codeValue = objectValue.get("status");
            }
            return fromText(String.valueOf(codeValue));
        }

        return fromText(String.valueOf(input));
    }

    public static ReviewStatus from(String value) {
        return fromText(value);
    }

    private static ReviewStatus fromText(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            throw InvalidReviewOperationException.missingRequirement("status");
        }

        String normalized = value.trim().toUpperCase();
        for (ReviewStatus status : values()) {
            if (status.name().equals(normalized) || status.code.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }

        throw InvalidReviewOperationException.invalidStatus(value, "PUBLISHED,HIDDEN");
    }
}
