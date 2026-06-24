package com.example.new_toy_store.review.domain;

public enum ReviewStatus {
    PUBLISHED("Đã hiển thị") {
        @Override
        public boolean isVisibleToPublic() {
            return true;
        }
    },
    HIDDEN("Bị ẩn bởi Admin") {
        @Override
        public boolean isVisibleToPublic() {
            return false;
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

    public static ReviewStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái đánh giá không được để trống");
        }
        try {
            return ReviewStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái đánh giá không hợp lệ: " + value + ". Chỉ chấp nhận PUBLISHED hoặc HIDDEN.");
        }
    }
}