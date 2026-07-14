package com.example.new_toy_store.customer_return.api.advice;

import com.example.new_toy_store.customer_return.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.customer_return.api")
public class CustomerReturnExceptionHandler {

    @ExceptionHandler(CustomerReturnNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CustomerReturnNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of("returnId", ex.getReturnId()));
    }

    @ExceptionHandler(DuplicateReturnRequestException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateReturnRequestException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), Map.of("orderId", ex.getOrderId(), "conflictType", "ACTIVE_DUPLICATE"));
    }

    @ExceptionHandler(ReturnRequestDeletedConflictException.class)
    public ResponseEntity<Map<String, Object>> handleDeletedConflict(ReturnRequestDeletedConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), Map.of("orderId", ex.getOrderId(), "conflictType", "SOFT_DELETED", "actionSuggested", "RESTORE"));
    }

    @ExceptionHandler(CustomerReturnAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(CustomerReturnAccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getContext());
    }

    @ExceptionHandler(InvalidCustomerReturnTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransition(InvalidCustomerReturnTransitionException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getContext());
    }

    @ExceptionHandler(InvalidCustomerReturnDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidData(InvalidCustomerReturnDataException ex) {
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