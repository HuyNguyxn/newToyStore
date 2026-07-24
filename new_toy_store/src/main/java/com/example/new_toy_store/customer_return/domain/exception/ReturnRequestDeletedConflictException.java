package com.example.new_toy_store.customer_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReturnRequestDeletedConflictException extends RuntimeException {

    private final Integer orderId;

    public ReturnRequestDeletedConflictException(Integer orderId) {
        super("Đơn hàng ID " + orderId + " có yêu cầu trả hàng đã bị xóa mềm. Vui lòng khôi phục bản ghi cũ thay vì tạo mới.");
        this.orderId = orderId;
    }

    public String getErrorType() {
        return "SOFT_DELETED_CUSTOMER_RETURN_CONFLICT";
    }

    public Integer getOrderId() {
        return orderId;
    }

    public Map<String, Object> getContextData() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("orderId", orderId);
        context.put("entity", "CustomerReturn");
        context.put("conflictType", "SOFT_DELETED_CONFLICT");
        context.put("suggestedAction", "RESTORE_DELETED_RECORD");
        return context;
    }
}
