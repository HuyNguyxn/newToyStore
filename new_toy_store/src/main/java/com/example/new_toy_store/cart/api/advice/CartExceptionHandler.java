package com.example.new_toy_store.cart.api.advice;

import com.example.new_toy_store.cart.domain.exception.*;
import com.example.new_toy_store.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.cart.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CartExceptionHandler {

    @ExceptionHandler({CartNotFoundException.class, CartItemNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            CartDomainException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(CartAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            CartAccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex, request);
    }

    @ExceptionHandler(CartDataConflictException.class)
    public ResponseEntity<ErrorResponse> handleDataConflictException(
            CartDataConflictException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(InvalidCartOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(
            InvalidCartOperationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(CartCrossModuleException.class)
    public ResponseEntity<ErrorResponse> handleCrossModuleException(
            CartCrossModuleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, CartDomainException ex, HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                status.value(),
                ex.getErrorType(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContextData()
        );
        return ResponseEntity.status(status).body(response);
    }
}