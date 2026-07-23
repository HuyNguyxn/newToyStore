package com.example.new_toy_store.product.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidProductOperationException extends ProductDomainException {

    public InvalidProductOperationException(String message) {
        this(HttpStatus.BAD_REQUEST, "Thao tác sản phẩm không hợp lệ", message);
    }

    private InvalidProductOperationException(HttpStatus status, String errorType, String message) {
        this(status, errorType, message, Map.of());
    }

    private InvalidProductOperationException(
            HttpStatus status,
            String errorType,
            String message,
            Map<String, ?> contextData
    ) {
        super(status, errorType, message, contextData);
    }

    public static InvalidProductOperationException emptyName() {
        return new InvalidProductOperationException(
                "Dữ liệu đầu vào không hợp lệ",
                "Tên sản phẩm không được để trống",
                Map.of("field", "name")
        );
    }

    public static InvalidProductOperationException negativePrice() {
        return new InvalidProductOperationException(
                "Dữ liệu đầu vào không hợp lệ",
                "Giá bán không được âm",
                Map.of("field", "price")
        );
    }

    public static InvalidProductOperationException emptyStatus() {
        return new InvalidProductOperationException(
                "Dữ liệu enum không hợp lệ",
                "Trạng thái sản phẩm không được để trống",
                Map.of("enum", "ProductStatus")
        );
    }

    public static InvalidProductOperationException invalidStatus(String value) {
        return new InvalidProductOperationException(
                "Dữ liệu enum không hợp lệ",
                "Trạng thái sản phẩm không hợp lệ: " + value,
                Map.of("enum", "ProductStatus", "value", safe(value))
        );
    }

    public static InvalidProductOperationException invalidStatusTransition(String currentStatus, String targetStatus) {
        return new InvalidProductOperationException(
                HttpStatus.CONFLICT,
                "Xung đột trạng thái sản phẩm",
                "Không thể chuyển trạng thái sản phẩm từ " + currentStatus + " sang " + targetStatus,
                Map.of("currentStatus", safe(currentStatus), "targetStatus", safe(targetStatus))
        );
    }

    public static InvalidProductOperationException emptyVariantType() {
        return new InvalidProductOperationException(
                "Dữ liệu enum không hợp lệ",
                "Loại biến thể không được để trống",
                Map.of("enum", "VariantType")
        );
    }

    public static InvalidProductOperationException invalidVariantType(String value) {
        return new InvalidProductOperationException(
                "Dữ liệu enum không hợp lệ",
                "Loại biến thể không hợp lệ: " + value,
                Map.of("enum", "VariantType", "value", safe(value))
        );
    }

    public static InvalidProductOperationException negativeInitialStock() {
        return new InvalidProductOperationException(
                "Dữ liệu tồn kho không hợp lệ",
                "Số lượng tồn kho ban đầu không được âm",
                Map.of("field", "initialStock")
        );
    }

    public static InvalidProductOperationException invalidStockAmount() {
        return new InvalidProductOperationException(
                "Dữ liệu tồn kho không hợp lệ",
                "Số lượng thao tác phải lớn hơn 0",
                Map.of("field", "amount")
        );
    }

    public static InvalidProductOperationException insufficientStock() {
        return new InvalidProductOperationException(
                HttpStatus.CONFLICT,
                "Xung đột tồn kho",
                "Số lượng tồn kho không đủ để thực hiện giao dịch"
        );
    }

    public static InvalidProductOperationException emptyImageUrl() {
        return new InvalidProductOperationException(
                "Dữ liệu hình ảnh không hợp lệ",
                "Đường dẫn hình ảnh không được để trống",
                Map.of("field", "imageUrl")
        );
    }

    public static InvalidProductOperationException invalidImage(Integer imageId) {
        return new InvalidProductOperationException(
                "Dữ liệu hình ảnh không hợp lệ",
                "ID hình ảnh " + imageId + " không thuộc về sản phẩm này",
                Map.of("imageId", imageId)
        );
    }

    public static InvalidProductOperationException emptyAttributeName() {
        return new InvalidProductOperationException(
                "Dữ liệu thuộc tính không hợp lệ",
                "Tên thuộc tính không được để trống",
                Map.of("field", "attributeName")
        );
    }

    public static InvalidProductOperationException emptyAttributeValue() {
        return new InvalidProductOperationException(
                "Dữ liệu thuộc tính không hợp lệ",
                "Giá trị thuộc tính không được để trống",
                Map.of("field", "attributeValue")
        );
    }

    public static InvalidProductOperationException invalidVariantTransition(String currentType, String targetType) {
        return new InvalidProductOperationException(
                HttpStatus.CONFLICT,
                "Xung đột loại biến thể",
                "Không thể chuyển loại biến thể từ " + currentType + " sang " + targetType,
                Map.of("currentType", safe(currentType), "targetType", safe(targetType))
        );
    }

    public static InvalidProductOperationException cannotAddAttributes(String variantType) {
        return new InvalidProductOperationException(
                HttpStatus.CONFLICT,
                "Lỗi logic nghiệp vụ sản phẩm",
                "Không thể thêm thuộc tính vào biến thể loại " + variantType,
                Map.of("variantType", safe(variantType))
        );
    }

    public static InvalidProductOperationException invalidImportData() {
        return new InvalidProductOperationException(
                "Dữ liệu nhập kho không hợp lệ",
                "Số lượng và giá nhập kho phải hợp lệ"
        );
    }

    public static InvalidProductOperationException supplierInactive(String statusName) {
        return new InvalidProductOperationException(
                HttpStatus.CONFLICT,
                "Xung đột dữ liệu ngoài module",
                "Nhà cung cấp đang ở trạng thái " + statusName + ", không thể liên kết với sản phẩm",
                Map.of("module", "supplier", "supplierStatus", safe(statusName))
        );
    }

    public static InvalidProductOperationException invalidCategories() {
        return new InvalidProductOperationException(
                "Dữ liệu ngoài module không hợp lệ",
                "Một hoặc nhiều ID danh mục không tồn tại trong hệ thống",
                Map.of("module", "category")
        );
    }

    public static InvalidProductOperationException variantNotFound() {
        return new InvalidProductOperationException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy biến thể sản phẩm",
                "Không tìm thấy mẫu mã sản phẩm yêu cầu"
        );
    }

    public static InvalidProductOperationException batchNotFound(String batchNumber) {
        return new InvalidProductOperationException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy lô hàng",
                "Không tìm thấy mã lô " + batchNumber + " trong kho sản phẩm",
                Map.of("batchNumber", safe(batchNumber))
        );
    }

    public static InvalidProductOperationException insufficientBatchStock(String batchNumber, int currentQty, int requestedQty) {
        return new InvalidProductOperationException(
                HttpStatus.CONFLICT,
                "Xung đột tồn kho theo lô",
                "Lô hàng " + batchNumber + " không đủ số lượng",
                Map.of("batchNumber", safe(batchNumber), "currentQuantity", currentQty, "requestedQuantity", requestedQty)
        );
    }

    public static InvalidProductOperationException accessDenied(String action, Integer productId) {
        return new InvalidProductOperationException(
                HttpStatus.FORBIDDEN,
                "Không có quyền thao tác sản phẩm",
                "Bạn không có quyền thực hiện thao tác " + action + " trên sản phẩm này",
                Map.of("action", safe(action), "productId", productId == null ? "" : productId)
        );
    }

    public static InvalidProductOperationException softDeletedConflict(String entityName, Integer entityId) {
        return new InvalidProductOperationException(
                HttpStatus.CONFLICT,
                "Xung đột dữ liệu đã xóa mềm",
                entityName + " đã bị xóa mềm nên không thể tiếp tục thao tác",
                Map.of("entity", safe(entityName), "entityId", entityId == null ? "" : entityId)
        );
    }

    public static InvalidProductOperationException duplicateActiveData(String field, String value) {
        return new InvalidProductOperationException(
                HttpStatus.CONFLICT,
                "Trùng lặp dữ liệu đang hoạt động",
                "Dữ liệu " + field + " đang được sử dụng bởi một bản ghi đang hoạt động",
                Map.of("field", safe(field), "value", safe(value))
        );
    }

    public static InvalidProductOperationException crossModuleDataMismatch(String module, String reason) {
        return new InvalidProductOperationException(
                "Gửi sai dữ liệu giữa module",
                "Dữ liệu gửi sang module " + module + " không hợp lệ: " + reason,
                Map.of("module", safe(module), "reason", safe(reason))
        );
    }

    private InvalidProductOperationException(String errorType, String message) {
        this(HttpStatus.BAD_REQUEST, errorType, message);
    }

    private InvalidProductOperationException(String errorType, String message, Map<String, ?> contextData) {
        this(HttpStatus.BAD_REQUEST, errorType, message, contextData);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
