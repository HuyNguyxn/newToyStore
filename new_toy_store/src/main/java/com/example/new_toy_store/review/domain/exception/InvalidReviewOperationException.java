package com.example.new_toy_store.review.domain.exception;

import java.util.Map;

public class InvalidReviewOperationException extends RuntimeException {
    private final String field;
    private final String invalidValue;
    private final String errorType;

    private InvalidReviewOperationException(String message, String field, String invalidValue, String errorType) {
        super(message);
        this.field = field;
        this.invalidValue = invalidValue;
        this.errorType = errorType;
    }

    public static InvalidReviewOperationException invalidRating(int rating) {
        return new InvalidReviewOperationException(
                "Điểm số đánh giá phải nằm trong khoảng từ 1 đến 5 sao",
                "rating",
                String.valueOf(rating),
                "INVALID_RATING"
        );
    }

    public static InvalidReviewOperationException invalidStatus(String status, String acceptedValues) {
        return new InvalidReviewOperationException(
                String.format("Trạng thái đánh giá không hợp lệ. Chỉ chấp nhận: %s", acceptedValues),
                "status",
                status,
                "INVALID_STATUS"
        );
    }

    public static InvalidReviewOperationException invalidStatusTransition(String currentStatus, String nextStatus) {
        return new InvalidReviewOperationException(
                String.format("Không thể chuyển trạng thái đánh giá từ %s sang %s", currentStatus, nextStatus),
                "statusTransition",
                currentStatus + "->" + nextStatus,
                "INVALID_STATUS_TRANSITION"
        );
    }

    public static InvalidReviewOperationException prohibitedContent(String fieldName) {
        return new InvalidReviewOperationException(
                "Nội dung đánh giá chứa từ ngữ vi phạm tiêu chuẩn cộng đồng. Vui lòng chỉnh sửa lại.",
                fieldName,
                "PROHIBITED_CONTENT",
                "PROHIBITED_CONTENT"
        );
    }

    public static InvalidReviewOperationException emptyReply() {
        return new InvalidReviewOperationException(
                "Nội dung phản hồi không được để trống",
                "adminReply",
                "null/empty",
                "EMPTY_ADMIN_REPLY"
        );
    }

    public static InvalidReviewOperationException timeWindowExpired(int days) {
        return new InvalidReviewOperationException(
                String.format("Đã quá thời hạn %d ngày để đánh giá sản phẩm này kể từ khi nhận hàng.", days),
                "timeWindow",
                String.valueOf(days),
                "REVIEW_TIME_WINDOW_EXPIRED"
        );
    }

    public static InvalidReviewOperationException missingRequirement(String fieldName) {
        return new InvalidReviewOperationException(
                String.format("Trường dữ liệu %s không được để trống", fieldName),
                fieldName,
                "null",
                "MISSING_REQUIRED_FIELD"
        );
    }

    public Map<String, ?> getContextData() {
        return Map.of(
                "field", field,
                "invalidValue", invalidValue,
                "errorType", errorType
        );
    }

    public String getField() {
        return field;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    public String getErrorType() {
        return errorType;
    }
}
