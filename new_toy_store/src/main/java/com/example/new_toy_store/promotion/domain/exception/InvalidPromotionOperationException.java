package com.example.new_toy_store.promotion.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidPromotionOperationException extends PromotionDomainException {

    private final String operation;
    private final Object invalidValue;

    private InvalidPromotionOperationException(
            HttpStatus status,
            String errorCode,
            String message,
            String operation,
            Object invalidValue
    ) {
        super(
                status,
                errorCode,
                message,
                buildContext(operation, invalidValue)
        );
        this.operation = operation;
        this.invalidValue = invalidValue;
    }

    private static Map<String, Object> buildContext(String operation, Object invalidValue) {
        if (invalidValue == null) {
            return Map.of("operation", operation);
        }
        return Map.of("operation", operation, "invalidValue", invalidValue);
    }

    public static InvalidPromotionOperationException nullScope() {
        return badRequest(
                "PROMOTION_SCOPE_REQUIRED",
                "Phạm vi khuyến mãi không được để trống.",
                "PARSE_SCOPE",
                null
        );
    }

    public static InvalidPromotionOperationException invalidScope(String value) {
        return badRequest(
                "PROMOTION_INVALID_SCOPE",
                "Phạm vi khuyến mãi không hợp lệ: " + value + ".",
                "PARSE_SCOPE",
                value
        );
    }

    public static InvalidPromotionOperationException missingTargetProduct() {
        return badRequest(
                "PROMOTION_TARGET_PRODUCT_REQUIRED",
                "Khuyến mãi cấp sản phẩm bắt buộc phải có ID sản phẩm mục tiêu.",
                "VALIDATE_SCOPE",
                null
        );
    }

    public static InvalidPromotionOperationException invalidTargetProductForOrder() {
        return badRequest(
                "PROMOTION_ORDER_SCOPE_TARGET_FORBIDDEN",
                "Khuyến mãi cấp đơn hàng không được gắn với một sản phẩm cụ thể.",
                "VALIDATE_SCOPE",
                "targetProductId"
        );
    }

    public static InvalidPromotionOperationException invalidTargetProductForShipping() {
        return badRequest(
                "PROMOTION_SHIPPING_SCOPE_TARGET_FORBIDDEN",
                "Khuyến mãi phí vận chuyển không được gắn với một sản phẩm cụ thể.",
                "VALIDATE_SCOPE",
                "targetProductId"
        );
    }

    public static InvalidPromotionOperationException nullType() {
        return badRequest(
                "PROMOTION_TYPE_REQUIRED",
                "Loại khuyến mãi không được để trống.",
                "PARSE_TYPE",
                null
        );
    }

    public static InvalidPromotionOperationException invalidType(String value) {
        return badRequest(
                "PROMOTION_INVALID_TYPE",
                "Loại khuyến mãi không hợp lệ: " + value + ".",
                "PARSE_TYPE",
                value
        );
    }

    public static InvalidPromotionOperationException nullPromoCode() {
        return badRequest(
                "PROMOTION_CODE_REQUIRED",
                "Mã khuyến mãi không được để trống.",
                "VALIDATE_PROMOTION",
                null
        );
    }

    public static InvalidPromotionOperationException nullPromoName() {
        return badRequest(
                "PROMOTION_NAME_REQUIRED",
                "Tên chương trình không được để trống.",
                "VALIDATE_PROMOTION",
                null
        );
    }

    public static InvalidPromotionOperationException nullPromoTypeOrScope() {
        return badRequest(
                "PROMOTION_TYPE_OR_SCOPE_REQUIRED",
                "Loại và phạm vi khuyến mãi không được để trống.",
                "VALIDATE_PROMOTION",
                null
        );
    }

    public static InvalidPromotionOperationException invalidDateRange() {
        return badRequest(
                "PROMOTION_INVALID_DATE_RANGE",
                "Ngày bắt đầu không được vượt quá ngày kết thúc.",
                "VALIDATE_DATE",
                null
        );
    }

    public static InvalidPromotionOperationException negativeDiscountValue(double value) {
        return badRequest(
                "PROMOTION_NEGATIVE_DISCOUNT",
                "Giá trị giảm giá không được âm.",
                "VALIDATE_VALUE",
                value
        );
    }

    public static InvalidPromotionOperationException percentageExceeded(double value) {
        return badRequest(
                "PROMOTION_PERCENTAGE_EXCEEDED",
                "Giảm giá phần trăm không được vượt quá 100%.",
                "VALIDATE_PERCENTAGE",
                value
        );
    }

    public static InvalidPromotionOperationException negativeUsedCount(int value) {
        return badRequest(
                "PROMOTION_NEGATIVE_USED_COUNT",
                "Số lượt sử dụng không được âm.",
                "VALIDATE_VALUE",
                value
        );
    }

    public static InvalidPromotionOperationException invalidUsageLimit(int currentUsedCount) {
        return badRequest(
                "PROMOTION_INVALID_USAGE_LIMIT",
                "Giới hạn lượt sử dụng không được nhỏ hơn số lượt đã áp dụng (" + currentUsedCount + ").",
                "SETUP_LIMIT",
                currentUsedCount
        );
    }

    public static InvalidPromotionOperationException quotaExceeded() {
        return conflict(
                "PROMOTION_QUOTA_EXCEEDED",
                "Chương trình khuyến mãi đã hết hiệu lực hoặc đã đạt giới hạn lượt sử dụng tối đa.",
                "INCREMENT_QUOTA",
                null
        );
    }

    public static InvalidPromotionOperationException quotaZero() {
        return conflict(
                "PROMOTION_USAGE_ALREADY_ZERO",
                "Số lượt sử dụng đã bằng 0, không thể hoàn trả thêm.",
                "DECREMENT_QUOTA",
                null
        );
    }

    public static InvalidPromotionOperationException codeExists(String code) {
        return conflict(
                "DUPLICATE_ACTIVE_PROMOTION_CODE",
                "Mã khuyến mãi " + code + " đã tồn tại trong hệ thống.",
                "CREATE_PROMOTION",
                code
        );
    }

    public static InvalidPromotionOperationException scopeMismatch() {
        return badRequest(
                "PROMOTION_SCOPE_MISMATCH",
                "Mã khuyến mãi không áp dụng cho tổng đơn hàng.",
                "CALCULATE_DISCOUNT",
                null
        );
    }

    public static InvalidPromotionOperationException minOrderNotMet() {
        return conflict(
                "PROMOTION_MIN_ORDER_NOT_MET",
                "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã khuyến mãi này.",
                "CALCULATE_DISCOUNT",
                null
        );
    }

    private static InvalidPromotionOperationException badRequest(
            String errorCode,
            String message,
            String operation,
            Object invalidValue
    ) {
        return new InvalidPromotionOperationException(HttpStatus.BAD_REQUEST, errorCode, message, operation, invalidValue);
    }

    private static InvalidPromotionOperationException conflict(
            String errorCode,
            String message,
            String operation,
            Object invalidValue
    ) {
        return new InvalidPromotionOperationException(HttpStatus.CONFLICT, errorCode, message, operation, invalidValue);
    }

    public String getOperation() { return operation; }
    public Object getInvalidValue() { return invalidValue; }
}
