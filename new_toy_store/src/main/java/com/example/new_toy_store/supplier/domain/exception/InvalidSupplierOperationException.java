package com.example.new_toy_store.supplier.domain.exception;

import java.util.HashMap;
import java.util.Map;

public class InvalidSupplierOperationException extends RuntimeException {
    private final String errorType;
    private final Map<String, Object> contextData;

    private InvalidSupplierOperationException(String message, String errorType, Map<String, Object> contextData) {
        super(message);
        this.errorType = errorType;
        this.contextData = contextData;
    }

    public String getErrorType() { return errorType; }
    public Map<String, Object> getContextData() { return contextData; }

    public static InvalidSupplierOperationException emptyField(String fieldName) {
        Map<String, Object> context = new HashMap<>();
        context.put("fieldName", fieldName);
        return new InvalidSupplierOperationException(
                "Trường dữ liệu không hợp lệ: " + fieldName + " không được để trống.",
                "EMPTY_FIELD", context);
    }

    public static InvalidSupplierOperationException invalidStatus(String invalidValue) {
        Map<String, Object> context = new HashMap<>();
        context.put("invalidValue", invalidValue);
        return new InvalidSupplierOperationException(
                "Trạng thái nhà cung cấp không hợp lệ: '" + invalidValue + "'.",
                "INVALID_STATUS", context);
    }

    public static InvalidSupplierOperationException stillActive(Integer id) {
        Map<String, Object> context = new HashMap<>();
        context.put("supplierId", id);
        return new InvalidSupplierOperationException(
                "Nhà cung cấp ID " + id + " vẫn đang hoạt động, thao tác khôi phục bị từ chối.",
                "INVALID_STATE_TRANSITION", context);
    }
}