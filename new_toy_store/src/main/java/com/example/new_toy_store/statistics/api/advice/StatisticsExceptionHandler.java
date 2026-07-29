package com.example.new_toy_store.statistics.api.advice;

import com.example.new_toy_store.global.exception.ErrorResponse;
import com.example.new_toy_store.statistics.domain.StatisticGroupBy;
import com.example.new_toy_store.statistics.domain.exception.InvalidStatisticGroupByException;
import com.example.new_toy_store.statistics.domain.exception.InvalidStatisticRequestException;
import com.example.new_toy_store.statistics.domain.exception.StatisticsDomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.example.new_toy_store.statistics.api")
public class StatisticsExceptionHandler {

    @ExceptionHandler(StatisticsDomainException.class)
    public ResponseEntity<ErrorResponse> handleStatisticsDomainException(
            StatisticsDomainException ex,
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        if (ex.getRequiredType() == StatisticGroupBy.class) {
            return handleStatisticsDomainException(
                    new InvalidStatisticGroupByException(
                            String.valueOf(ex.getValue()),
                            Arrays.stream(StatisticGroupBy.values()).map(Enum::name).toList()
                    ),
                    request
            );
        }
        return handleStatisticsDomainException(
                new InvalidStatisticRequestException(ex.getName(), String.valueOf(ex.getValue())),
                request
        );
    }
}
