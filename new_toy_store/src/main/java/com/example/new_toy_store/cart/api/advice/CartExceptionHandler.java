package com.example.new_toy_store.cart.api.advice;

import com.example.new_toy_store.cart.domain.exception.CartItemNotFoundException;
import com.example.new_toy_store.cart.domain.exception.CartNotFoundException;
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
    public ResponseEntity<ErrorResponse> handleCartNotFoundExceptions(
            RuntimeException ex,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Không tìm thấy dữ liệu",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}