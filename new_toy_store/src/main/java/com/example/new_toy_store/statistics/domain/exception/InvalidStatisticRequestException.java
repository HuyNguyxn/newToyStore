package com.example.new_toy_store.statistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidStatisticRequestException extends StatisticsDomainException {

    public InvalidStatisticRequestException(String parameter, String value) {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_STATISTIC_REQUEST",
                "Statistic request parameter is invalid.",
                Map.of("parameter", parameter == null ? "" : parameter, "value", value == null ? "" : value)
        );
    }
}
