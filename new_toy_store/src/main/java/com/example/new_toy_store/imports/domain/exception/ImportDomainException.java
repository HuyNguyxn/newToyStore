package com.example.new_toy_store.imports.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

public abstract class ImportDomainException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Map<String, Object> contextData;

    protected ImportDomainException(
            HttpStatus status,
            String errorCode,
            String message,
            Map<String, Object> contextData
    ) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.contextData = contextData == null ? Map.of() : Collections.unmodifiableMap(contextData);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getContextData() {
        return contextData;
    }
}
