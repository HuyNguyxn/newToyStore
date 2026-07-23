package com.example.new_toy_store.product.domain.exception;

public class InvalidProductOperationException extends RuntimeException {

    public InvalidProductOperationException(String message) {
        super(message);
    }

    public static InvalidProductOperationException emptyName() {
        return new InvalidProductOperationException("Tên sản phẩm không được để trống");
    }

    public static InvalidProductOperationException negativePrice() {
        return new InvalidProductOperationException("Giá bán không được âm");
    }

    public static InvalidProductOperationException emptyStatus() {
        return new InvalidProductOperationException("Trạng thái sản phẩm không được để trống");
    }

    public static InvalidProductOperationException invalidStatus(String value) {
        return new InvalidProductOperationException("Trạng thái sản phẩm không hợp lệ: " + value);
    }

    public static InvalidProductOperationException invalidStatusTransition(String currentStatus, String targetStatus) {
        return new InvalidProductOperationException("Không thể chuyển trạng thái sản phẩm từ " + currentStatus + " sang " + targetStatus);
    }

    public static InvalidProductOperationException emptyVariantType() {
        return new InvalidProductOperationException("Loại biến thể không được để trống");
    }

    public static InvalidProductOperationException invalidVariantType(String value) {
        return new InvalidProductOperationException("Loại biến thể không hợp lệ: " + value);
    }

    public static InvalidProductOperationException negativeInitialStock() {
        return new InvalidProductOperationException("Số lượng tồn kho ban đầu không được âm");
    }

    public static InvalidProductOperationException invalidStockAmount() {
        return new InvalidProductOperationException("Số lượng thao tác phải lớn hơn 0");
    }

    public static InvalidProductOperationException insufficientStock() {
        return new InvalidProductOperationException("Số lượng tồn kho không đủ để thực hiện giao dịch");
    }

    public static InvalidProductOperationException emptyImageUrl() {
        return new InvalidProductOperationException("Đường dẫn hình ảnh không được để trống");
    }

    public static InvalidProductOperationException invalidImage(Integer imageId) {
        return new InvalidProductOperationException("ID hình ảnh " + imageId + " không thuộc về sản phẩm này");
    }

    public static InvalidProductOperationException emptyAttributeName() {
        return new InvalidProductOperationException("Tên thuộc tính không được để trống");
    }

    public static InvalidProductOperationException emptyAttributeValue() {
        return new InvalidProductOperationException("Giá trị thuộc tính không được để trống");
    }

    public static InvalidProductOperationException invalidVariantTransition(String currentType, String targetType) {
        return new InvalidProductOperationException("Không thể chuyển từ " + currentType + " sang " + targetType);
    }

    public static InvalidProductOperationException cannotAddAttributes(String variantType) {
        return new InvalidProductOperationException("Không thể thêm thuộc tính vào biến thể loại " + variantType);
    }

    public static InvalidProductOperationException invalidImportData() {
        return new InvalidProductOperationException("Số lượng và giá nhập kho phải hợp lệ");
    }

    public static InvalidProductOperationException supplierInactive(String statusName) {
        return new InvalidProductOperationException("Nhà cung cấp hiện đang ở trạng thái: " + statusName + ". Không thể liên kết hoặc thao tác.");
    }

    public static InvalidProductOperationException invalidCategories() {
        return new InvalidProductOperationException("Một hoặc nhiều ID danh mục không tồn tại trong hệ thống");
    }

    public static InvalidProductOperationException variantNotFound() {
        return new InvalidProductOperationException("Không tìm thấy mẫu mã sản phẩm yêu cầu");
    }

    public static InvalidProductOperationException batchNotFound(String batchNumber) {
        return new InvalidProductOperationException("Không tìm thấy mã lô: " + batchNumber + " trong kho sản phẩm.");
    }

    public static InvalidProductOperationException insufficientBatchStock(String batchNumber, int currentQty, int requestedQty) {
        return new InvalidProductOperationException("Lô hàng " + batchNumber + " không đủ số lượng (Hiện có: " + currentQty + ", Yêu cầu trừ: " + requestedQty + ").");
    }
}
