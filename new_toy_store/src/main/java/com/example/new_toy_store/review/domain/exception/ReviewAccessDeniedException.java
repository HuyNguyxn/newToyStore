package com.example.new_toy_store.review.domain.exception;

public class ReviewAccessDeniedException extends RuntimeException {
    private final Integer userId;
    private final String reason;

    private ReviewAccessDeniedException(String message, Integer userId, String reason) {
        super(message);
        this.userId = userId;
        this.reason = reason;
    }

    public static ReviewAccessDeniedException notOwner(Integer userId) {
        return new ReviewAccessDeniedException("Thao tác bị từ chối: Bạn không có quyền chỉnh sửa hoặc xóa đánh giá của người khác.", userId, "NOT_OWNER");
    }

    public static ReviewAccessDeniedException userAccountLocked(Integer userId, String currentStatus) {
        return new ReviewAccessDeniedException(String.format("Thao tác bị từ chối. Tài khoản của bạn hiện đang ở trạng thái: %s", currentStatus), userId, "ACCOUNT_LOCKED");
    }

    public static ReviewAccessDeniedException userNotFound(Integer userId) {
        return new ReviewAccessDeniedException("Không tìm thấy thông tin khách hàng trên hệ thống", userId, "USER_NOT_FOUND");
    }

    public Integer getUserId() { return userId; }
    public String getReason() { return reason; }
}