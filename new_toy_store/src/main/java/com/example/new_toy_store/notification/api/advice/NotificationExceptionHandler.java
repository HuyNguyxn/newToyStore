package com.example.new_toy_store.notification.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.notification.domain.exception.NotificationDomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.notification.api")
public class NotificationExceptionHandler {

    @ExceptionHandler(NotificationDomainException.class)
    public ResponseEntity<ErrorResponse> handleNotificationDomainException(
            NotificationDomainException ex,
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
