package com.example.new_toy_store.category.api.advice;

import com.example.new_toy_store.category.api.CategoryController;
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

@RestControllerAdvice(assignableTypes = CategoryController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CategoryDataIntegrityExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "CATEGORY_DATA_CONFLICT",
                "Dữ liệu danh mục xung đột với ràng buộc hiện có. Vui lòng kiểm tra slug, parent hoặc dữ liệu liên quan.",
                request.getRequestURI(),
                Map.of("reason", "constraint")
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
