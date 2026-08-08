package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class InvalidSupplierReturnOperationException extends RuntimeException {

    private final String errorType;
    private final Map<String, Object> contextData;

    private InvalidSupplierReturnOperationException(String message, String errorType, Map<String, Object> contextData) {
        super(message);
        this.errorType = errorType;
        this.contextData = contextData;
    }

    public String getErrorType() {
        return errorType;
    }

    public Map<String, Object> getContextData() {
        return contextData;
    }

    public static InvalidSupplierReturnOperationException emptyField(String fieldName) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", fieldName);
        context.put("validation", "required");
        return new InvalidSupplierReturnOperationException(
                "Trường bắt buộc không được để trống: " + fieldName + ".",
                "SUPPLIER_RETURN_EMPTY_FIELD",
                context
        );
    }

    public static InvalidSupplierReturnOperationException invalidStatus(String invalidValue) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", "status");
        context.put("invalidValue", invalidValue);
        context.put("enumName", "SupplierReturnStatus");
        return new InvalidSupplierReturnOperationException(
                "Trạng thái phiếu trả hàng nhà cung cấp không hợp lệ: '" + invalidValue + "'.",
                "SUPPLIER_RETURN_INVALID_STATUS",
                context
        );
    }

    public static InvalidSupplierReturnOperationException invalidReason(String invalidValue) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", "reason");
        context.put("invalidValue", invalidValue);
        context.put("enumName", "SupplierReturnReason");
        return new InvalidSupplierReturnOperationException(
                "Lý do trả hàng nhà cung cấp không hợp lệ: '" + invalidValue + "'.",
                "SUPPLIER_RETURN_INVALID_REASON",
                context
        );
    }

    public static InvalidSupplierReturnOperationException negativeFinancialValue() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fields", "shippingFee, taxAmount, discountAmount");
        context.put("validation", "mustBeGreaterThanOrEqualZero");
        return new InvalidSupplierReturnOperationException(
                "Các giá trị tài chính như phí vận chuyển, thuế hoặc chiết khấu không được là số âm.",
                "SUPPLIER_RETURN_NEGATIVE_FINANCIAL_VALUE",
                context
        );
    }

    public static InvalidSupplierReturnOperationException invalidQuantity() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", "quantity");
        context.put("minValue", 1);
        return new InvalidSupplierReturnOperationException(
                "Số lượng xuất trả phải lớn hơn 0.",
                "SUPPLIER_RETURN_INVALID_QUANTITY",
                context
        );
    }

    public static InvalidSupplierReturnOperationException emptyItems() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", "items");
        context.put("minRequired", 1);
        return new InvalidSupplierReturnOperationException(
                "Không thể xử lý phiếu trả hàng nhà cung cấp khi chưa có sản phẩm nào.",
                "SUPPLIER_RETURN_EMPTY_ITEMS",
                context
        );
    }

    public static InvalidSupplierReturnOperationException invalidTransition(String currentState, String nextState) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("currentState", currentState);
        context.put("attemptedState", nextState);
        context.put("businessRule", "SupplierReturnStatus.canTransitionTo");
        return new InvalidSupplierReturnOperationException(
                "Không thể chuyển trạng thái phiếu trả hàng nhà cung cấp từ '" + currentState + "' sang '" + nextState + "'.",
                "SUPPLIER_RETURN_INVALID_STATE_TRANSITION",
                context
        );
    }

    public static InvalidSupplierReturnOperationException readOnlyState(String action, String currentState) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("action", action);
        context.put("currentState", currentState);
        return new InvalidSupplierReturnOperationException(
                "Không thể thực hiện thao tác '" + action + "' vì phiếu trả hàng nhà cung cấp đang bị khóa ở trạng thái '" + currentState + "'.",
                "SUPPLIER_RETURN_READ_ONLY_STATE",
                context
        );
    }

    public static InvalidSupplierReturnOperationException invalidAcceptedQuantity(int maxQty) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", "acceptedQuantity");
        context.put("minValue", 0);
        context.put("maxAllowedQuantity", maxQty);
        return new InvalidSupplierReturnOperationException(
                "Số lượng nhà cung cấp chấp nhận không hợp lệ. Giá trị phải nằm trong khoảng từ 0 đến " + maxQty + ".",
                "SUPPLIER_RETURN_INVALID_ACCEPTED_QUANTITY",
                context
        );
    }

    public static InvalidSupplierReturnOperationException quantityExceedsImported(Integer variantId, int requested, int imported) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("variantId", variantId);
        context.put("requestedQuantity", requested);
        context.put("importedQuantity", imported);
        return new InvalidSupplierReturnOperationException(
                "Số lượng trả vượt quá số lượng đã nhập của biến thể " + variantId + ".",
                "SUPPLIER_RETURN_QUANTITY_EXCEEDS_IMPORTED",
                context
        );
    }
}
