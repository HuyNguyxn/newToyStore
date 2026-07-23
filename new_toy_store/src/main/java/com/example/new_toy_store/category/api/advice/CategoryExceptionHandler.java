package com.example.new_toy_store.category.api.advice;

import com.example.new_toy_store.category.domain.exception.CategoryDomainException;
import com.example.new_toy_store.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CategoryExceptionHandler {

    @ExceptionHandler(CategoryDomainException.class)
    public ResponseEntity<ErrorResponse> handleCategoryDomainException(
            CategoryDomainException ex,
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
}
