package com.example.new_toy_store.cart.api.advice;

import com.example.new_toy_store.cart.domain.exception.CartAccessDeniedException;
import com.example.new_toy_store.cart.domain.exception.CartCrossModuleException;
import com.example.new_toy_store.cart.domain.exception.CartDataConflictException;
import com.example.new_toy_store.cart.domain.exception.CartDomainException;
import com.example.new_toy_store.cart.domain.exception.CartItemNotFoundException;
import com.example.new_toy_store.cart.domain.exception.CartNotFoundException;
import com.example.new_toy_store.cart.domain.exception.InvalidCartDataException;
import com.example.new_toy_store.cart.domain.exception.InvalidCartOperationException;
import com.example.new_toy_store.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

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

    @ExceptionHandler(InvalidCartDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataException(
            InvalidCartDataException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(CartCrossModuleException.class)
    public ResponseEntity<ErrorResponse> handleCrossModuleException(
            CartCrossModuleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "CART_DATA_CONFLICT",
                "Dữ liệu giỏ hàng xung đột với dữ liệu hiện có. Vui lòng kiểm tra lại thông tin giỏ hàng.",
                request.getRequestURI(),
                Map.of("reason", "constraint")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
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
