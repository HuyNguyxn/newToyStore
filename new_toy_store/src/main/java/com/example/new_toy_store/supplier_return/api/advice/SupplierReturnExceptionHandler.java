package com.example.new_toy_store.supplier_return.api.advice;

import com.example.new_toy_store.supplier_return.api.SupplierReturnController;
import com.example.new_toy_store.supplier_return.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = SupplierReturnController.class)
public class SupplierReturnExceptionHandler {

    @ExceptionHandler(InvalidSupplierReturnOperationException.class)
    public ResponseEntity<Object> handleInvalidOperation(
            InvalidSupplierReturnOperationException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getErrorType(),
                ex.getMessage(),
                ex.getContextData(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(SupplierReturnNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(
            SupplierReturnNotFoundException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "SUPPLIER_RETURN_NOT_FOUND",
                ex.getMessage(),
                ex.getContext(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(SupplierReturnAccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(
            SupplierReturnAccessDeniedException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                ex.getMessage(),
                ex.getContext(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DuplicateSupplierReturnException.class)
    public ResponseEntity<Object> handleDuplicate(
            DuplicateSupplierReturnException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "DUPLICATE_ACTIVE_RECORD",
                ex.getMessage(),
                ex.getContext(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(SupplierReturnDeletedConflictException.class)
    public ResponseEntity<Object> handleDeletedConflict(
            SupplierReturnDeletedConflictException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "SOFT_DELETED_CONFLICT",
                ex.getMessage(),
                ex.getContext(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<Object> buildResponse(
            HttpStatus status,
            String errorType,
            String message,
            Map<String, Object> context,
            String path) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("errorType", errorType);
        body.put("message", message);
        body.put("path", path);

        if (context != null && !context.isEmpty()) {
            body.put("context", context);
        }

        return new ResponseEntity<>(body, status);
    }
}