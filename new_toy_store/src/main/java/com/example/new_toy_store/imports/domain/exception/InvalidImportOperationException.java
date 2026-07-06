package com.example.new_toy_store.imports.domain.exception;

public class InvalidImportOperationException extends RuntimeException {

    private InvalidImportOperationException(String message) {
        super(message);
    }

    public static InvalidImportOperationException supplierInactive(String statusName) {
        return new InvalidImportOperationException("Nhà cung cấp hiện đang " + statusName + ". Không thể tạo phiếu nhập.");
    }

    public static InvalidImportOperationException invalidProducts() {
        return new InvalidImportOperationException("Một hoặc nhiều ID sản phẩm không tồn tại trong hệ thống.");
    }

    public static InvalidImportOperationException invalidVariant(Integer variantId, String productName) {
        return new InvalidImportOperationException("Mã mẫu mã (ID: " + variantId + ") không thuộc về sản phẩm: " + productName);
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

    public static InvalidImportOperationException emptyStatus() {
        return new InvalidImportOperationException("Trạng thái phiếu nhập không được để trống.");
    }

    public static InvalidImportOperationException invalidStatus(String value) {
        return new InvalidImportOperationException("Trạng thái phiếu nhập không hợp lệ: " + value);
    }
}