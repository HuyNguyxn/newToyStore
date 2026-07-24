package com.example.new_toy_store.review.domain.exception;

public class InvalidReviewOperationException extends RuntimeException {
    private final String field;
    private final String invalidValue;

    private InvalidReviewOperationException(String message, String field, String invalidValue) {
        super(message);
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public static InvalidReviewOperationException invalidRating(int rating) {
        return new InvalidReviewOperationException(
                "Điểm số đánh giá phải nằm trong khoảng từ 1 đến 5 sao",
                "rating",
                String.valueOf(rating)
        );
    }

    public static InvalidReviewOperationException invalidStatus(String status, String acceptedValues) {
        return new InvalidReviewOperationException(
                String.format("Trạng thái đánh giá không hợp lệ. Chỉ chấp nhận: %s", acceptedValues),
                "status",
                status
        );
    }

    public static InvalidReviewOperationException invalidStatusTransition(String currentStatus, String nextStatus) {
        return new InvalidReviewOperationException(
                String.format("Không thể chuyển trạng thái đánh giá từ %s sang %s", currentStatus, nextStatus),
                "statusTransition",
                currentStatus + "->" + nextStatus
        );
    }

    public static InvalidReviewOperationException emptyReply() {
        return new InvalidReviewOperationException(
                "Nội dung phản hồi không được để trống",
                "adminReply",
                "null/empty"
        );
    }

    public static InvalidReviewOperationException timeWindowExpired(int days) {
        return new InvalidReviewOperationException(
                String.format("Đã quá thời hạn %d ngày để đánh giá sản phẩm này kể từ khi nhận hàng.", days),
                "timeWindow",
                String.valueOf(days)
        );
    }

    public static InvalidReviewOperationException missingRequirement(String fieldName) {
        return new InvalidReviewOperationException(
                String.format("Trường dữ liệu %s không được để trống", fieldName),
                fieldName,
                "null"
        );
    }

    public String getField() {
        return field;
    }

    public String getInvalidValue() {
        return invalidValue;
    }
}
