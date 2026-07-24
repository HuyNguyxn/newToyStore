package com.example.new_toy_store.customer_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomerReturnNotFoundException extends RuntimeException {

    private final Integer returnId;

    public CustomerReturnNotFoundException(Integer returnId) {
        super("Không tìm thấy yêu cầu trả hàng của khách với ID: " + returnId + ".");
        this.returnId = returnId;
    }

    public String getErrorType() {
        return "CUSTOMER_RETURN_NOT_FOUND";
    }

    public Integer getReturnId() {
        return returnId;
    }

    public Map<String, Object> getContextData() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("returnId", returnId);
        context.put("entity", "CustomerReturn");
        return context;
    }
}
