package com.example.new_toy_store.user.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.user.domain.exception.UserDomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.user.api")
public class UserExceptionHandler {

    @ExceptionHandler(UserDomainException.class)
    public ResponseEntity<ErrorResponse> handleUserDomainException(
            UserDomainException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                ex.getStatus().value(),
                ex.getErrorType(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getContextData()
        );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
