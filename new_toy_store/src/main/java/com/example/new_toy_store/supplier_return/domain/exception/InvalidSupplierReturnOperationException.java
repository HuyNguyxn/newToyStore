package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.HashMap;
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
        Map<String, Object> context = new HashMap<>();
        context.put("fieldName", fieldName);
        return new InvalidSupplierReturnOperationException(
                "Trường bắt buộc không được để trống: " + fieldName,
                "EMPTY_FIELD",
                context
        );
    }

    public static InvalidSupplierReturnOperationException invalidStatus(String invalidValue) {
        Map<String, Object> context = new HashMap<>();
        context.put("invalidValue", invalidValue);
        return new InvalidSupplierReturnOperationException(
                "Trạng thái Phiếu trả hàng không hợp lệ: '" + invalidValue + "'.",
                "INVALID_ENUM_STATUS",
                context
        );
    }

    public static InvalidSupplierReturnOperationException invalidReason(String invalidValue) {
        Map<String, Object> context = new HashMap<>();
        context.put("invalidValue", invalidValue);
        return new InvalidSupplierReturnOperationException(
                "Lý do trả hàng không hợp lệ: '" + invalidValue + "'.",
                "INVALID_ENUM_REASON",
                context
        );
    }

    public static InvalidSupplierReturnOperationException negativeFinancialValue() {
        return new InvalidSupplierReturnOperationException(
                "Các giá trị tài chính (Phí, Thuế, Chiết khấu) không được là số âm.",
                "NEGATIVE_FINANCIAL_VALUE",
                new HashMap<>()
        );
    }

    public static InvalidSupplierReturnOperationException invalidQuantity() {
        return new InvalidSupplierReturnOperationException(
                "Số lượng xuất trả phải lớn hơn 0.",
                "INVALID_QUANTITY",
                new HashMap<>()
        );
    }

    public static InvalidSupplierReturnOperationException emptyItems() {
        return new InvalidSupplierReturnOperationException(
                "Không thể xử lý Phiếu trả hàng không có sản phẩm nào.",
                "EMPTY_ITEMS",
                new HashMap<>()
        );
    }

    public static InvalidSupplierReturnOperationException invalidTransition(String currentState, String nextState) {
        Map<String, Object> context = new HashMap<>();
        context.put("currentState", currentState);
        context.put("attemptedState", nextState);
        return new InvalidSupplierReturnOperationException(
                "Vi phạm máy trạng thái: Không thể chuyển từ [" + currentState + "] sang [" + nextState + "].",
                "INVALID_STATE_TRANSITION",
                context
        );
    }

    public static InvalidSupplierReturnOperationException readOnlyState(String action, String currentState) {
        Map<String, Object> context = new HashMap<>();
        context.put("action", action);
        context.put("currentState", currentState);
        return new InvalidSupplierReturnOperationException(
                "Từ chối thao tác: " + action + ". Phiếu đang bị khóa ở trạng thái: " + currentState,
                "READ_ONLY_VIOLATION",
                context
        );
    }
}