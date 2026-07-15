package com.example.new_toy_store.customer_return.domain.exception;

import java.util.Map;

public class InvalidCustomerReturnOperationException extends RuntimeException {
    private final String errorType;
    private final Map<String, Object> contextData;

    private InvalidCustomerReturnOperationException(String message, String errorType, Map<String, Object> contextData) {
        super(message);
        this.errorType = errorType;
        this.contextData = contextData;
    }

    public String getErrorType() { return errorType; }
    public Map<String, Object> getContextData() { return contextData; }

    public static InvalidCustomerReturnOperationException invalidDisputeState(String currentState) {
        return new InvalidCustomerReturnOperationException(
                "Không thể mở khiếu nại. Yêu cầu trả hàng đang ở trạng thái: " + currentState + ". Chỉ hỗ trợ khiếu nại khi bị Từ chối hoặc Kiểm định thất bại.",
                "INVALID_DISPUTE_STATE", Map.of("currentState", currentState)
        );
    }

    public static InvalidCustomerReturnOperationException notInDisputeState(String currentState) {
        return new InvalidCustomerReturnOperationException(
                "Không thể giải quyết khiếu nại. Đơn này không ở trạng thái Tranh chấp/Khiếu nại (hiện tại: " + currentState + ").",
                "NOT_IN_DISPUTE_STATE", Map.of("currentState", currentState)
        );
    }
}