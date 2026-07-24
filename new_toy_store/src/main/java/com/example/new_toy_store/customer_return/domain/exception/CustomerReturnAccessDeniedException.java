package com.example.new_toy_store.customer_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomerReturnAccessDeniedException extends RuntimeException {

    private final String username;
    private final String action;

    public CustomerReturnAccessDeniedException(String username, String action) {
        super("Tài khoản '" + username + "' không có quyền thực hiện thao tác '" + action + "' trên yêu cầu trả hàng.");
        this.username = username;
        this.action = action;
    }

    public String getErrorType() {
        return "CUSTOMER_RETURN_ACCESS_DENIED";
    }

    public Map<String, Object> getContextData() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("username", username);
        context.put("action", action);
        context.put("requiredPermission", "CUSTOMER_RETURN_MANAGE");
        return context;
    }
}
