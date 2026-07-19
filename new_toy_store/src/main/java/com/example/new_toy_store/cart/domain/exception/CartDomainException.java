package com.example.new_toy_store.cart.domain.exception;

import java.util.HashMap;
import java.util.Map;

public abstract class CartDomainException extends RuntimeException {

    private final String errorType;
    private final Map<String, Object> contextData = new HashMap<>();

    protected CartDomainException(String message, String errorType) {
        super(message);
        this.errorType = errorType;
    }

    public void addContext(String key, Object value) {
        if (value != null) {
            this.contextData.put(key, value);
        }
    }

    public String getErrorType() { return errorType; }
    public Map<String, Object> getContextData() { return contextData; }
}