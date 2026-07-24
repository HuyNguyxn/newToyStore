package com.example.new_toy_store.moderation.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.moderation.domain.exception.BlacklistedWordNotFoundException;
import com.example.new_toy_store.moderation.domain.exception.InvalidModerationOperationException;
import com.example.new_toy_store.moderation.domain.exception.ModerationAccessDeniedException;
import com.example.new_toy_store.moderation.domain.exception.ModerationConflictException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.moderation")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ModerationExceptionHandler {

    @ExceptionHandler(BlacklistedWordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(BlacklistedWordNotFoundException ex,
                                                                 HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Không tìm thấy dữ liệu",
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContextData()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidModerationOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(InvalidModerationOperationException ex,
                                                                         HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrorType(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContextData()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ModerationConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ModerationConflictException ex,
                                                                 HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Xung đột dữ liệu",
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContextData()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ModerationAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(ModerationAccessDeniedException ex,
                                                                     HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Từ chối quyền truy cập",
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContextData()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
