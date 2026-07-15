package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.HashMap;
import java.util.Map;

public class SupplierReturnAccessDeniedException extends RuntimeException {

    private final String username;
    private final String action;

    public SupplierReturnAccessDeniedException(String username, String action) {
        super("Truy cập bị từ chối: Tài khoản '" + username + "' không có quyền thực hiện hành động '" + action + "'.");
        this.username = username;
        this.action = action;
    }

    public Map<String, Object> getContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("username", username);
        context.put("action", action);
        return context;
    }
}