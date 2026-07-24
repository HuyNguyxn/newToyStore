package com.example.new_toy_store.imports.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.imports.domain.exception.ImportDomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.imports.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ImportExceptionHandler {

    @ExceptionHandler(ImportDomainException.class)
    public ResponseEntity<ErrorResponse> handleImportDomainException(
            ImportDomainException ex,
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "IMPORT_DATA_CONFLICT",
                "Dữ liệu phiếu nhập xung đột với ràng buộc hiện có. Vui lòng kiểm tra nhà cung cấp, sản phẩm, biến thể hoặc item bị trùng.",
                request.getRequestURI(),
                Map.of("reason", "constraint")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
