package com.example.new_toy_store.customer_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class DuplicateReturnRequestException extends RuntimeException {

    private final Integer orderId;

    public DuplicateReturnRequestException(Integer orderId) {
        super("Đơn hàng ID " + orderId + " đã có một yêu cầu trả hàng đang hoạt động.");
        this.orderId = orderId;
    }

    public String getErrorType() {
        return "DUPLICATE_ACTIVE_CUSTOMER_RETURN";
    }

    public Integer getOrderId() {
        return orderId;
    }

    public Map<String, Object> getContextData() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("orderId", orderId);
        context.put("entity", "CustomerReturn");
        context.put("conflictType", "ACTIVE_DUPLICATE");
        return context;
    }
}
