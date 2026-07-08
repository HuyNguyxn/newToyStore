package com.example.new_toy_store.promotion.domain.exception;

public class InvalidPromotionOperationException extends RuntimeException {

    private final String operation;
    private final Object invalidValue;

    private InvalidPromotionOperationException(String message, String operation, Object invalidValue) {
        super(message);
        this.operation = operation;
        this.invalidValue = invalidValue;
    }

    public static InvalidPromotionOperationException nullScope() {
        return new InvalidPromotionOperationException(
                "Phạm vi khuyến mãi không được để trống.",
                "PARSE_SCOPE",
                null
        );
    }

    public static InvalidPromotionOperationException invalidScope(String value) {
        return new InvalidPromotionOperationException(
                "Phạm vi không hợp lệ: " + value,
                "PARSE_SCOPE",
                value
        );
    }

    public static InvalidPromotionOperationException missingTargetProduct() {
        return new InvalidPromotionOperationException(
                "Khuyến mãi cấp sản phẩm bắt buộc phải có ID sản phẩm mục tiêu.",
                "VALIDATE_SCOPE",
                null
        );
    }

    public static InvalidPromotionOperationException invalidTargetProductForOrder() {
        return new InvalidPromotionOperationException(
                "Khuyến mãi cấp đơn hàng không được gắn với một sản phẩm cụ thể.",
                "VALIDATE_SCOPE",
                null
        );
    }

    public static InvalidPromotionOperationException invalidTargetProductForShipping() {
        return new InvalidPromotionOperationException(
                "Khuyến mãi phí vận chuyển không được gắn với một sản phẩm cụ thể.",
                "VALIDATE_SCOPE",
                null
        );
    }

    public static InvalidPromotionOperationException nullType() {
        return new InvalidPromotionOperationException(
                "Loại khuyến mãi không được để trống.",
                "PARSE_TYPE",
                null
        );
    }

    public static InvalidPromotionOperationException invalidType(String value) {
        return new InvalidPromotionOperationException(
                "Loại khuyến mãi không hợp lệ: " + value,
                "PARSE_TYPE",
                value
        );
    }

    public static InvalidPromotionOperationException nullPromoCode() {
        return new InvalidPromotionOperationException(
                "Mã khuyến mãi không được để trống.",
                "VALIDATE_PROMOTION",
                null
        );
    }

    public static InvalidPromotionOperationException nullPromoName() {
        return new InvalidPromotionOperationException(
                "Tên chương trình không được để trống.",
                "VALIDATE_PROMOTION",
                null
        );
    }

    public static InvalidPromotionOperationException nullPromoTypeOrScope() {
        return new InvalidPromotionOperationException(
                "Loại và phạm vi khuyến mãi không được để trống.",
                "VALIDATE_PROMOTION",
                null
        );
    }

    public static InvalidPromotionOperationException invalidDateRange() {
        return new InvalidPromotionOperationException(
                "Ngày bắt đầu không được vượt quá ngày kết thúc.",
                "VALIDATE_DATE",
                null
        );
    }

    public static InvalidPromotionOperationException negativeDiscountValue(double value) {
        return new InvalidPromotionOperationException(
                "Giá trị giảm giá không được âm.",
                "VALIDATE_VALUE",
                value
        );
    }

    public static InvalidPromotionOperationException percentageExceeded(double value) {
        return new InvalidPromotionOperationException(
                "Giảm giá phần trăm không được vượt quá 100%.",
                "VALIDATE_PERCENTAGE",
                value
        );
    }

    public static InvalidPromotionOperationException negativeUsedCount(int value) {
        return new InvalidPromotionOperationException(
                "Số lượt sử dụng không được âm.",
                "VALIDATE_VALUE",
                value
        );
    }

    public static InvalidPromotionOperationException invalidUsageLimit(int currentUsedCount) {
        return new InvalidPromotionOperationException(
                "Giới hạn lượt sử dụng không được nhỏ hơn số lượt đã được áp dụng (" + currentUsedCount + ").",
                "SETUP_LIMIT",
                currentUsedCount
        );
    }

    public static InvalidPromotionOperationException quotaExceeded() {
        return new InvalidPromotionOperationException(
                "Chương trình khuyến mãi đã hết hiệu lực hoặc đã đạt giới hạn lượt sử dụng tối đa.",
                "INCREMENT_QUOTA",
                null
        );
    }

    public static InvalidPromotionOperationException quotaZero() {
        return new InvalidPromotionOperationException(
                "Số lượt sử dụng đã bằng 0, không thể hoàn trả thêm.",
                "DECREMENT_QUOTA",
                null
        );
    }

    public static InvalidPromotionOperationException codeExists(String code) {
        return new InvalidPromotionOperationException(
                "Mã khuyến mãi đã tồn tại trong hệ thống.",
                "CREATE_PROMOTION",
                code
        );
    }

    public static InvalidPromotionOperationException scopeMismatch() {
        return new InvalidPromotionOperationException(
                "Mã khuyến mãi không áp dụng cho tổng đơn hàng.",
                "CALCULATE_DISCOUNT",
                null
        );
    }

    public static InvalidPromotionOperationException minOrderNotMet() {
        return new InvalidPromotionOperationException(
                "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã khuyến mãi này.",
                "CALCULATE_DISCOUNT",
                null
        );
    }

    public String getOperation() { return operation; }
    public Object getInvalidValue() { return invalidValue; }
}