package com.example.new_toy_store.cart.domain.exception;

public class InvalidCartOperationException extends CartDomainException {

    private InvalidCartOperationException(String message, String operation, Object invalidValue) {
        super(message, "INVALID_CART_OPERATION");
        addContext("operation", operation);
        addContext("invalidValue", invalidValue);
    }

    public static InvalidCartOperationException nullUserId() {
        return new InvalidCartOperationException(
                "ID người dùng không được để trống khi khởi tạo giỏ hàng.",
                "CREATE_CART",
                null
        );
    }

    public static InvalidCartOperationException invalidUserId(Integer userId) {
        return new InvalidCartOperationException(
                "ID người dùng phải lớn hơn 0.",
                "CREATE_CART",
                userId
        );
    }

    public static InvalidCartOperationException nullProductOrVariant() {
        return new InvalidCartOperationException(
                "ID sản phẩm và ID phân loại không được để trống.",
                "ADD_ITEM",
                null
        );
    }

    public static InvalidCartOperationException invalidProductOrVariant(Integer productId, Integer variantId) {
        InvalidCartOperationException exception = new InvalidCartOperationException(
                "ID sản phẩm và ID phân loại phải lớn hơn 0.",
                "ADD_ITEM",
                null
        );
        exception.addContext("productId", productId);
        exception.addContext("variantId", variantId);
        return exception;
    }

    public static InvalidCartOperationException invalidQuantity(int quantity) {
        return new InvalidCartOperationException(
                "Số lượng không hợp lệ (" + quantity + "). Số lượng thao tác phải lớn hơn 0.",
                "UPDATE_QUANTITY",
                quantity
        );
    }

    public static InvalidCartOperationException invalidPrice(double price) {
        return new InvalidCartOperationException(
                "Giá sản phẩm trong giỏ phải là số hữu hạn và không được nhỏ hơn 0.",
                "ADD_ITEM",
                price
        );
    }

    public static InvalidCartOperationException maxItemsExceeded(int maxItems) {
        return new InvalidCartOperationException(
                "Giỏ hàng đã đạt giới hạn tối đa " + maxItems + " loại mặt hàng. Vui lòng thanh toán bớt.",
                "ADD_ITEM",
                maxItems
        );
    }

    public static InvalidCartOperationException invalidStatusTransition(String currentStatus, String nextStatus) {
        InvalidCartOperationException exception = new InvalidCartOperationException(
                "Luồng nghiệp vụ không hợp lệ: Không thể chuyển trạng thái giỏ hàng từ " + currentStatus + " sang " + nextStatus + ".",
                "CHANGE_STATUS",
                nextStatus
        );
        exception.addContext("currentStatus", currentStatus);
        exception.addContext("nextStatus", nextStatus);
        return exception;
    }

    public static InvalidCartOperationException cartNotActive() {
        return new InvalidCartOperationException(
                "Không thể chỉnh sửa giỏ hàng vì giỏ hàng đang trong quá trình thanh toán.",
                "CHECK_CART_STATUS",
                null
        );
    }

    public static InvalidCartOperationException checkoutNotInProgress() {
        return new InvalidCartOperationException(
                "Không thể hoàn tất checkout vì giỏ hàng chưa ở trạng thái đang thanh toán.",
                "COMPLETE_CHECKOUT",
                null
        );
    }

    public static InvalidCartOperationException emptyCart() {
        return new InvalidCartOperationException(
                "Giỏ hàng trống hoặc không có sản phẩm nào được chọn để thanh toán.",
                "CHECKOUT",
                0
        );
    }

    public static InvalidCartOperationException mergeExceedsLimit(int totalItems, int limit) {
        InvalidCartOperationException exception = new InvalidCartOperationException(
                "Gộp giỏ hàng thất bại: Tổng số lượng mặt hàng vượt quá giới hạn cho phép.",
                "MERGE_CART",
                totalItems
        );
        exception.addContext("totalItems", totalItems);
        exception.addContext("limit", limit);
        return exception;
    }
}
