package com.example.new_toy_store.customer_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class InvalidCustomerReturnDataException extends RuntimeException {

    private final String errorType;
    private final Map<String, Object> contextData;

    private InvalidCustomerReturnDataException(String message, String errorType, Map<String, Object> contextData) {
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

    public static InvalidCustomerReturnDataException emptyField(String fieldName) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", fieldName);
        context.put("validation", "required");
        return new InvalidCustomerReturnDataException(
                "Trường bắt buộc không được để trống: " + fieldName + ".",
                "CUSTOMER_RETURN_EMPTY_FIELD",
                context
        );
    }

    public static InvalidCustomerReturnDataException invalidStatus(String invalidValue) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", "status");
        context.put("invalidValue", invalidValue);
        context.put("enumName", "CustomerReturnStatus");
        return new InvalidCustomerReturnDataException(
                "Trạng thái trả hàng của khách không hợp lệ: '" + invalidValue + "'.",
                "CUSTOMER_RETURN_INVALID_STATUS",
                context
        );
    }

    public static InvalidCustomerReturnDataException invalidReason(String invalidValue) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", "reasonCode");
        context.put("invalidValue", invalidValue);
        context.put("enumName", "ReturnReasonCode");
        return new InvalidCustomerReturnDataException(
                "Lý do trả hàng của khách không hợp lệ: '" + invalidValue + "'.",
                "CUSTOMER_RETURN_INVALID_REASON",
                context
        );
    }

    public static InvalidCustomerReturnDataException missingProofImage() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("fieldName", "proofImages");
        context.put("minRequired", 1);
        context.put("reason", "DAMAGED_OR_WRONG_ITEM");
        return new InvalidCustomerReturnDataException(
                "Bắt buộc cung cấp ít nhất 1 hình ảnh chứng minh khi trả hàng do lỗi sản phẩm hoặc giao sai hàng.",
                "CUSTOMER_RETURN_MISSING_PROOF_IMAGE",
                context
        );
    }

    public static InvalidCustomerReturnDataException invalidOrderStatus(String status) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("currentOrderStatus", status);
        context.put("requiredStatus", "COMPLETED");
        context.put("sourceModule", "order");
        context.put("targetModule", "customer_return");
        return new InvalidCustomerReturnDataException(
                "Không thể tạo yêu cầu trả hàng vì đơn hàng đang ở trạng thái '" + status + "'. Chỉ đơn hàng COMPLETED mới được yêu cầu trả hàng.",
                "CUSTOMER_RETURN_INVALID_ORDER_STATUS",
                context
        );
    }

    public static InvalidCustomerReturnDataException invalidRefundAmount(Integer returnId, double refundAmount) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("returnId", returnId);
        context.put("refundAmount", refundAmount);
        context.put("validation", "greater_than_zero");
        return new InvalidCustomerReturnDataException(
                "Số tiền hoàn của phiếu trả hàng #" + returnId + " phải lớn hơn 0.",
                "CUSTOMER_RETURN_INVALID_REFUND_AMOUNT",
                context
        );
    }
}
