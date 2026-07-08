package com.example.new_toy_store.promotion.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.promotion.domain.exception.InvalidPromotionOperationException;
import com.example.new_toy_store.promotion.domain.exception.PromotionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.promotion.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PromotionExceptionHandler {

    @ExceptionHandler(PromotionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePromotionNotFoundException(
            PromotionNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Không tìm thấy dữ liệu",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidPromotionOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPromotionOperationException(
            InvalidPromotionOperationException ex,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Thao tác không hợp lệ",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}