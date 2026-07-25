package com.example.new_toy_store.logistics.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.logistics.domain.exception.LogisticsDomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.example.new_toy_store.logistics.api")
public class LogisticsExceptionHandler {

    @ExceptionHandler(LogisticsDomainException.class)
    public ResponseEntity<ErrorResponse> handleLogisticsDomainException(
            LogisticsDomainException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                ex.getStatus().value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContextData()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "LOGISTICS_DATA_CONFLICT",
                "Shipment data conflicts with existing constraints. Please check order or tracking code.",
                request.getRequestURI(),
                Map.of("reason", "constraint")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
