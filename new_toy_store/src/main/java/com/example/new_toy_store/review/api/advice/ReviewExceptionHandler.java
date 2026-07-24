package com.example.new_toy_store.review.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.review.domain.exception.InvalidReviewOperationException;
import com.example.new_toy_store.review.domain.exception.ReviewAccessDeniedException;
import com.example.new_toy_store.review.domain.exception.ReviewConflictException;
import com.example.new_toy_store.review.domain.exception.ReviewNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.review")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReviewExceptionHandler {

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ReviewNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Không tìm thấy dữ liệu",
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContextData()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidReviewOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidReviewOperationException ex,
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

    @ExceptionHandler(ReviewConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ReviewConflictException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Xung đột dữ liệu",
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContextData()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ReviewAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(ReviewAccessDeniedException ex,
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
