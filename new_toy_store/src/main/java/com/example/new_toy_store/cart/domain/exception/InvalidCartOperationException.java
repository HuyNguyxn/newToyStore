package com.example.new_toy_store.cart.domain.exception;

public class InvalidCartOperationException extends RuntimeException {

    private final String operation;
    private final Object invalidValue;

    private InvalidCartOperationException(String message, String operation, Object invalidValue) {
        super(message);
        this.operation = operation;
        this.invalidValue = invalidValue;
    }

    public static InvalidCartOperationException nullUserId() {
        return new InvalidCartOperationException(
                "ID người dùng không được để trống khi khởi tạo giỏ hàng.",
                "CREATE_CART",
                null
        );
    }

    public static InvalidCartOperationException nullProductOrVariant() {
        return new InvalidCartOperationException(
                "ID sản phẩm và ID phân loại không được để trống.",
                "ADD_ITEM",
                null
        );
    }

    public static InvalidCartOperationException invalidQuantity(int quantity) {
        return new InvalidCartOperationException(
                "Số lượng không hợp lệ (" + quantity + "). Số lượng thao tác phải lớn hơn 0.",
                "UPDATE_QUANTITY",
                quantity
        );
    }

    public static InvalidCartOperationException maxItemsExceeded(int maxItems) {
        return new InvalidCartOperationException(
                "Giỏ hàng đã đạt giới hạn tối đa " + maxItems + " loại mặt hàng. Vui lòng thanh toán bớt.",
                "ADD_ITEM",
                maxItems
        );
    }

    public String getOperation() { return operation; }
    public Object getInvalidValue() { return invalidValue; }
}