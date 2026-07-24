package com.example.new_toy_store.customer_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class InvalidCustomerReturnOperationException extends RuntimeException {

    private final String errorType;
    private final Map<String, Object> contextData;

    private InvalidCustomerReturnOperationException(String message, String errorType, Map<String, Object> contextData) {
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

    public static InvalidCustomerReturnOperationException invalidDisputeState(String currentState) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("operation", "OPEN_DISPUTE");
        context.put("currentState", currentState);
        context.put("allowedStates", "REJECTED, INSPECTION_FAILED");
        return new InvalidCustomerReturnOperationException(
                "Không thể mở khiếu nại khi yêu cầu trả hàng đang ở trạng thái '" + currentState + "'. Chỉ hỗ trợ khiếu nại khi bị từ chối hoặc kiểm định thất bại.",
                "CUSTOMER_RETURN_INVALID_DISPUTE_STATE",
                context
        );
    }

    public static InvalidCustomerReturnOperationException notInDisputeState(String currentState) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("operation", "RESOLVE_DISPUTE");
        context.put("currentState", currentState);
        context.put("requiredState", "DISPUTED");
        return new InvalidCustomerReturnOperationException(
                "Không thể giải quyết khiếu nại vì yêu cầu trả hàng không ở trạng thái tranh chấp. Trạng thái hiện tại: '" + currentState + "'.",
                "CUSTOMER_RETURN_NOT_IN_DISPUTE_STATE",
                context
        );
    }
}
