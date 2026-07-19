package com.example.new_toy_store.cart.domain.exception;

public class InvalidCartOperationException extends CartDomainException {

    private InvalidCartOperationException(String message, String operation, Object invalidValue) {
        super(message, "INVALID_OPERATION");
        addContext("operation", operation);
        if (invalidValue != null) {
            addContext("invalidValue", invalidValue);
        }
    }

    public static InvalidCartOperationException nullUserId() {
        return new InvalidCartOperationException("ID người dùng không được để trống khi khởi tạo giỏ hàng.", "CREATE_CART", null);
    }

    public static InvalidCartOperationException nullProductOrVariant() {
        return new InvalidCartOperationException("ID sản phẩm và ID phân loại không được để trống.", "ADD_ITEM", null);
    }

    public static InvalidCartOperationException invalidQuantity(int quantity) {
        return new InvalidCartOperationException("Số lượng không hợp lệ (" + quantity + "). Số lượng thao tác phải lớn hơn 0.", "UPDATE_QUANTITY", quantity);
    }

    public static InvalidCartOperationException maxItemsExceeded(int maxItems) {
        return new InvalidCartOperationException("Giỏ hàng đã đạt giới hạn tối đa " + maxItems + " loại mặt hàng. Vui lòng thanh toán bớt.", "ADD_ITEM", maxItems);
    }

    public static InvalidCartOperationException invalidStatusTransition(String currentStatus, String nextStatus) {
        return new InvalidCartOperationException(
                String.format("Luồng phi logic: Không thể chuyển trạng thái giỏ hàng từ [%s] sang [%s]", currentStatus, nextStatus),
                "CHANGE_STATUS", nextStatus);
    }

    public static InvalidCartOperationException cartNotActive() {
        return new InvalidCartOperationException("Không thể chỉnh sửa: Giỏ hàng đang trong quá trình thanh toán.", "CHECK_CART_STATUS", null);
    }

    public static InvalidCartOperationException emptyCart() {
        return new InvalidCartOperationException("Giỏ hàng trống hoặc không có sản phẩm nào được chọn để thanh toán.", "CHECKOUT", 0);
    }

    public static InvalidCartOperationException mergeExceedsLimit(int totalItems, int limit) {
        return new InvalidCartOperationException(
                "Gộp giỏ hàng thất bại: Tổng số lượng mặt hàng (" + totalItems + ") vượt quá giới hạn cho phép (" + limit + ").",
                "MERGE_CART",
                totalItems
        );
    }
}