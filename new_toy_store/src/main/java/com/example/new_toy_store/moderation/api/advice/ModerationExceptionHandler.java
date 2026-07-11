package com.example.new_toy_store.moderation.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.moderation.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.moderation.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ModerationExceptionHandler {

    @ExceptionHandler(BlacklistedWordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(BlacklistedWordNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Không tìm thấy dữ liệu", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidModerationOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(InvalidModerationOperationException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Thao tác không hợp lệ", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ModerationConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ModerationConflictException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(HttpStatus.CONFLICT.value(), "Xung đột dữ liệu", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ModerationAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(ModerationAccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Lỗi phân quyền", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}