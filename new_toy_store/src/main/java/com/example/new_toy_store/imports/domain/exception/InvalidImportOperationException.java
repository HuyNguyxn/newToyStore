package com.example.new_toy_store.imports.domain.exception;

public class InvalidImportOperationException extends RuntimeException {

    private InvalidImportOperationException(String message) {
        super(message);
    }

    public static InvalidImportOperationException invalidStatusTransition(String action) {
        return new InvalidImportOperationException("Không thể " + action + " phiếu nhập ở trạng thái hiện tại.");
    }

    public static InvalidImportOperationException emptyItems() {
        return new InvalidImportOperationException("Không thể hoàn thành phiếu nhập khi không có sản phẩm nào.");
    }

    public static InvalidImportOperationException missingItemIds() {
        return new InvalidImportOperationException("ID sản phẩm và biến thể không được để trống.");
    }

    public static InvalidImportOperationException invalidQuantity() {
        return new InvalidImportOperationException("Số lượng phải lớn hơn 0.");
    }

    public static InvalidImportOperationException negativePrice() {
        return new InvalidImportOperationException("Giá nhập không được âm.");
    }
}