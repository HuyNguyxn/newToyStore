package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class SupplierReturnAccessDeniedException extends RuntimeException {

    private final String username;
    private final String action;

    public SupplierReturnAccessDeniedException(String username, String action) {
        super("Tài khoản '" + username + "' không có quyền thực hiện thao tác '" + action + "' trên phiếu trả hàng nhà cung cấp.");
        this.username = username;
        this.action = action;
    }

    public String getErrorType() {
        return "SUPPLIER_RETURN_ACCESS_DENIED";
    }

    public Map<String, Object> getContextData() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("username", username);
        context.put("action", action);
        context.put("requiredPermission", "SUPPLIER_RETURN_MANAGE");
        return context;
    }
}
