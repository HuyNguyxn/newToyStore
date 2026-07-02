package com.example.new_toy_store.imports.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.imports.domain.exception.ImportNoteNotFoundException;
import com.example.new_toy_store.imports.domain.exception.InvalidImportOperationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.imports.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ImportExceptionHandler {

    @ExceptionHandler(ImportNoteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ImportNoteNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Không tìm thấy dữ liệu", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InvalidImportOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidImportOperationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Thao tác không hợp lệ", ex.getMessage(), request.getRequestURI()));
    }
}