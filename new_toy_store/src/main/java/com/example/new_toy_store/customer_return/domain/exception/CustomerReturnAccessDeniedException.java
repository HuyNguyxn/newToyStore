package com.example.new_toy_store.customer_return.domain.exception;

import java.util.Map;

public class CustomerReturnAccessDeniedException extends RuntimeException {
    private final String username;
    private final String action;

    public CustomerReturnAccessDeniedException(String username, String action) {
        super("Tài khoản '" + username + "' không có quyền thực hiện hành động: " + action);
        this.username = username;
        this.action = action;
    }

    public Map<String, Object> getContext() {
        return Map.of("username", username, "action", action);
    }
}