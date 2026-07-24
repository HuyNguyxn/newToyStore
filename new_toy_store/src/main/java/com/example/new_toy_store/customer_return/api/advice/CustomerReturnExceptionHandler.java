package com.example.new_toy_store.customer_return.api.advice;

import com.example.new_toy_store.customer_return.domain.exception.CustomerReturnAccessDeniedException;
import com.example.new_toy_store.customer_return.domain.exception.CustomerReturnNotFoundException;
import com.example.new_toy_store.customer_return.domain.exception.DuplicateReturnRequestException;
import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnDataException;
import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnOperationException;
import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnTransitionException;
import com.example.new_toy_store.customer_return.domain.exception.ReturnRequestDeletedConflictException;
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
@RestControllerAdvice(basePackages = "com.example.new_toy_store.customer_return.api")
public class CustomerReturnExceptionHandler {

    @ExceptionHandler(CustomerReturnNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CustomerReturnNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(CustomerReturnAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(CustomerReturnAccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(DuplicateReturnRequestException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateReturnRequestException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(ReturnRequestDeletedConflictException.class)
    public ResponseEntity<Map<String, Object>> handleDeletedConflict(ReturnRequestDeletedConflictException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(InvalidCustomerReturnDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidData(InvalidCustomerReturnDataException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(InvalidCustomerReturnOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOperation(InvalidCustomerReturnOperationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
    }

    @ExceptionHandler(InvalidCustomerReturnTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransition(InvalidCustomerReturnTransitionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getErrorType(), ex.getMessage(), ex.getContextData(), request);
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
