package com.example.new_toy_store.review.domain.exception;

import java.util.Map;

public class ReviewConflictException extends RuntimeException {
    private final String conflictType;
    private final Integer orderItemId;

    private ReviewConflictException(String message, String conflictType, Integer orderItemId) {
        super(message);
        this.conflictType = conflictType;
        this.orderItemId = orderItemId;
    }

    public static ReviewConflictException duplicateReview(Integer orderItemId) {
        return new ReviewConflictException(
                "Bạn đã đánh giá sản phẩm này trong đơn hàng rồi. Vui lòng sử dụng tính năng chỉnh sửa.",
                "ACTIVE_DUPLICATE",
                orderItemId
        );
    }

    public static ReviewConflictException softDeletedConflict(Integer orderItemId) {
        return new ReviewConflictException(
                "Đánh giá cho sản phẩm này đã từng bị xóa. Không thể tạo mới trực tiếp trùng lặp.",
                "SOFT_DELETED_CONFLICT",
                orderItemId
        );
    }

    public Map<String, ?> getContextData() {
        return Map.of(
                "conflictType", conflictType,
                "orderItemId", orderItemId
        );
    }

    public String getConflictType() {
        return conflictType;
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }
}
