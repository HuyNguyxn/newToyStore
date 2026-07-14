package com.example.new_toy_store.customer_return.domain.exception;

import java.util.Map;

public class InvalidCustomerReturnDataException extends RuntimeException {
    private final String errorType;
    private final Map<String, Object> contextData;

    private InvalidCustomerReturnDataException(String message, String errorType, Map<String, Object> contextData) {
        super(message);
        this.errorType = errorType;
        this.contextData = contextData;
    }

    public String getErrorType() { return errorType; }
    public Map<String, Object> getContextData() { return contextData; }

    public static InvalidCustomerReturnDataException emptyField(String fieldName) {
        return new InvalidCustomerReturnDataException("Trường không được để trống: " + fieldName, "EMPTY_FIELD", Map.of("fieldName", fieldName));
    }

    public static InvalidCustomerReturnDataException invalidStatus(String invalidValue) {
        return new InvalidCustomerReturnDataException("Trạng thái trả hàng không hợp lệ: '" + invalidValue + "'.", "INVALID_ENUM_STATUS", Map.of("invalidValue", invalidValue));
    }

    public static InvalidCustomerReturnDataException invalidReason(String invalidValue) {
        return new InvalidCustomerReturnDataException("Lý do trả hàng không hợp lệ: '" + invalidValue + "'.", "INVALID_ENUM_REASON", Map.of("invalidValue", invalidValue));
    }

    public static InvalidCustomerReturnDataException missingProofImage() {
        return new InvalidCustomerReturnDataException("Bắt buộc phải cung cấp ít nhất 1 hình ảnh chứng minh đối với lý do Hàng lỗi/Giao sai.", "MISSING_PROOF_IMAGE", Map.of("minRequired", 1));
    }
}