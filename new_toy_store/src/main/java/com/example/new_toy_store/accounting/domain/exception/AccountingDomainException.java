package com.example.new_toy_store.accounting.domain.exception;

import java.util.Map;

public class AccountingDomainException extends RuntimeException {
    private final String code;
    private final Map<String, Object> context;

    public AccountingDomainException(String code, String message, Map<String, Object> context) {
        super(message);
        this.code = code;
        this.context = context == null ? Map.of() : Map.copyOf(context);
    }

    public String getCode() { return code; }
    public Map<String, Object> getContext() { return context; }
}
