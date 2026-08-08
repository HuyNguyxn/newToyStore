package com.example.new_toy_store.customer_payment.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.customer_payment.domain.exception.CustomerPaymentDomainException;
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
@RestControllerAdvice(basePackages = "com.example.new_toy_store.payment.api")
public class CustomerPaymentExceptionHandler {

    @ExceptionHandler(CustomerPaymentDomainException.class)
    public ResponseEntity<ErrorResponse> handlePaymentDomainException(
            CustomerPaymentDomainException ex,
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
                "PAYMENT_DATA_CONFLICT",
                "Payment data conflicts with existing constraints. Please check the order or active payment transaction.",
                request.getRequestURI(),
                Map.of("reason", "constraint")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
