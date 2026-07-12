package com.example.new_toy_store.supplier.api.advice;

import com.example.new_toy_store.supplier.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.supplier.api")
public class SupplierExceptionHandler {

    @ExceptionHandler(SupplierNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(SupplierNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of("supplierId", ex.getSupplierId()));
    }

    @ExceptionHandler(DuplicateSupplierException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateSupplierException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), Map.of("phoneNumber", ex.getPhoneNumber(), "conflictType", "ACTIVE"));
    }

    @ExceptionHandler(SupplierDeletedConflictException.class)
    public ResponseEntity<Map<String, Object>> handleDeletedConflict(SupplierDeletedConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), Map.of("phoneNumber", ex.getPhoneNumber(), "conflictType", "SOFT_DELETED", "actionSuggested", "RESTORE"));
    }

    @ExceptionHandler(SupplierAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(SupplierAccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), Map.of("deniedAction", ex.getAction(), "suggestion", "Vui lòng liên hệ Admin."));
    }

    @ExceptionHandler(InvalidSupplierOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperation(InvalidSupplierOperationException ex) {
        Map<String, Object> context = new HashMap<>();
        context.put("errorType", ex.getErrorType());
        if (ex.getContextData() != null) {
            context.putAll(ex.getContextData());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), context);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, Map<String, Object> context) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (context != null && !context.isEmpty()) {
            body.put("context", context);
        }
        return ResponseEntity.status(status).body(body);
    }
}