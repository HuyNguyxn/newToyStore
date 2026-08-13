package com.example.new_toy_store.imports.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidImportOperationException extends ImportDomainException {

    private InvalidImportOperationException(
            HttpStatus status,
            String errorCode,
            String message,
            Map<String, Object> contextData
    ) {
        super(status, errorCode, message, contextData);
    }

    public static InvalidImportOperationException supplierInactive(String statusName) {
        return new InvalidImportOperationException(
                HttpStatus.CONFLICT,
                "IMPORT_SUPPLIER_INACTIVE",
                "Nhà cung cấp hiện đang " + statusName + ". Không thể tạo phiếu nhập.",
                Map.of(
                        "supplierStatus", statusName,
                        "reason", "SUPPLIER_INACTIVE"
                )
        );
    }

    public static InvalidImportOperationException duplicateVariant() {
        return new InvalidImportOperationException(
                HttpStatus.BAD_REQUEST,
                "IMPORT_DUPLICATE_VARIANT",
                "Mỗi biến thể chỉ được xuất hiện một lần trong phiếu nhập; hãy gộp số lượng trước khi gửi.",
                Map.of("field", "items")
        );
    }

    public static InvalidImportOperationException invalidProducts() {
        return new InvalidImportOperationException(
                HttpStatus.BAD_REQUEST,
                "IMPORT_INVALID_PRODUCTS",
                "Một hoặc nhiều ID sản phẩm không tồn tại trong hệ thống.",
                Map.of(
                        "field", "items.productId",
                        "reason", "PRODUCT_NOT_FOUND"
                )
        );
    }

    public static InvalidImportOperationException invalidVariant(Integer variantId, String productName) {
        return new InvalidImportOperationException(
                HttpStatus.BAD_REQUEST,
                "IMPORT_INVALID_VARIANT",
                "Biến thể ID " + variantId + " không thuộc về sản phẩm " + productName + ".",
                Map.of(
                        "variantId", variantId,
                        "productName", productName,
                        "field", "items.variantId"
                )
        );
    }

    public static InvalidImportOperationException invalidStatusTransition(String action) {
        return new InvalidImportOperationException(
                HttpStatus.CONFLICT,
                "IMPORT_INVALID_STATUS_TRANSITION",
                "Không thể " + action + " phiếu nhập ở trạng thái hiện tại.",
                Map.of(
                        "action", action,
                        "reason", "INVALID_STATUS_TRANSITION"
                )
        );
    }

    public static InvalidImportOperationException emptyItems() {
        return new InvalidImportOperationException(
                HttpStatus.BAD_REQUEST,
                "IMPORT_EMPTY_ITEMS",
                "Không thể hoàn thành phiếu nhập khi chưa có sản phẩm nào.",
                Map.of(
                        "field", "items",
                        "reason", "EMPTY_ITEMS"
                )
        );
    }

    public static InvalidImportOperationException missingItemIds() {
        return new InvalidImportOperationException(
                HttpStatus.BAD_REQUEST,
                "IMPORT_ITEM_IDS_REQUIRED",
                "ID sản phẩm và ID biến thể không được để trống.",
                Map.of(
                        "fields", "productId,variantId",
                        "reason", "REQUIRED"
                )
        );
    }

    public static InvalidImportOperationException invalidQuantity() {
        return new InvalidImportOperationException(
                HttpStatus.BAD_REQUEST,
                "IMPORT_INVALID_QUANTITY",
                "Số lượng nhập phải lớn hơn 0.",
                Map.of(
                        "field", "quantity",
                        "reason", "MIN_VALUE"
                )
        );
    }

    public static InvalidImportOperationException negativePrice() {
        return new InvalidImportOperationException(
                HttpStatus.BAD_REQUEST,
                "IMPORT_NEGATIVE_PRICE",
                "Giá nhập không được âm.",
                Map.of(
                        "field", "importPrice",
                        "reason", "MIN_VALUE"
                )
        );
    }

    public static InvalidImportOperationException emptyStatus() {
        return new InvalidImportOperationException(
                HttpStatus.BAD_REQUEST,
                "IMPORT_STATUS_REQUIRED",
                "Trạng thái phiếu nhập không được để trống.",
                Map.of(
                        "field", "status",
                        "reason", "REQUIRED"
                )
        );
    }

    public static InvalidImportOperationException invalidStatus(String value) {
        return new InvalidImportOperationException(
                HttpStatus.BAD_REQUEST,
                "IMPORT_INVALID_STATUS",
                "Trạng thái phiếu nhập không hợp lệ: " + value + ".",
                Map.of(
                        "field", "status",
                        "rejectedValue", String.valueOf(value),
                        "allowedValues", "PENDING,COMPLETED,CANCELLED"
                )
        );
    }
}
