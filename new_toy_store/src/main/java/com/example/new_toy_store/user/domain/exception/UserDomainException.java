package com.example.new_toy_store.user.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public abstract class UserDomainException extends RuntimeException {

    private final HttpStatus status;
    private final String errorType;
    private final Map<String, ?> contextData;

    protected UserDomainException(HttpStatus status, String errorType, String message) {
        this(status, errorType, message, Map.of());
    }

    protected UserDomainException(
            HttpStatus status,
            String errorType,
            String message,
            Map<String, ?> contextData
    ) {
        super(message);
        this.status = status;
        this.errorType = errorType;
        this.contextData = contextData == null ? Map.of() : contextData;
    }

    public HttpStatus getStatus() { return status; }
    public String getErrorType() { return errorType; }
    public Map<String, ?> getContextData() { return contextData; }
}
