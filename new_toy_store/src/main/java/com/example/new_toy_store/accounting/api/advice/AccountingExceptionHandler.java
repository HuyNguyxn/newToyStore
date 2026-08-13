package com.example.new_toy_store.accounting.api.advice;

import com.example.new_toy_store.accounting.domain.exception.AccountingDomainException;
import com.example.new_toy_store.accounting.domain.exception.JournalEntryNotFoundException;
import com.example.new_toy_store.accounting.domain.exception.LedgerAccountNotFoundException;
import com.example.new_toy_store.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.new_toy_store.accounting.api")
public class AccountingExceptionHandler {
    @ExceptionHandler(AccountingDomainException.class)
    public ResponseEntity<ErrorResponse> handle(AccountingDomainException ex, HttpServletRequest request) {
        HttpStatus status = ex instanceof JournalEntryNotFoundException || ex instanceof LedgerAccountNotFoundException
                ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.value(), ex.getCode(), ex.getMessage(), request.getRequestURI(), ex.getContext()
        ));
    }
}
