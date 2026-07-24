package com.example.new_toy_store.supplier_return.api.advice;

import com.example.new_toy_store.supplier_return.domain.exception.DuplicateSupplierReturnException;
import com.example.new_toy_store.supplier_return.domain.exception.InvalidSupplierReturnOperationException;
import com.example.new_toy_store.supplier_return.domain.exception.SupplierReturnAccessDeniedException;
import com.example.new_toy_store.supplier_return.domain.exception.SupplierReturnDeletedConflictException;
import com.example.new_toy_store.supplier_return.domain.exception.SupplierReturnNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.example.new_toy_store.supplier_return.api")
public class SupplierReturnExceptionHandler {

    @ExceptionHandler(InvalidSupplierReturnOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperation(
            InvalidSupplierReturnOperationException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(SupplierReturnNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            SupplierReturnNotFoundException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(SupplierReturnAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            SupplierReturnAccessDeniedException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(DuplicateSupplierReturnException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(
            DuplicateSupplierReturnException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(SupplierReturnDeletedConflictException.class)
    public ResponseEntity<Map<String, Object>> handleDeletedConflict(
            SupplierReturnDeletedConflictException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String errorType,
            String message,
            Map<String, Object> context,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("errorType", errorType);
        body.put("message", message);
        body.put("path", request.getRequestURI());

        if (context != null && !context.isEmpty()) {
            body.put("context", context);
        }

        return ResponseEntity.status(status).body(body);
    }
}
