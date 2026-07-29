package com.example.new_toy_store.statistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class InvalidStatisticGroupByException extends StatisticsDomainException {

    public InvalidStatisticGroupByException(String value, List<String> allowedValues) {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_GROUP_BY",
                "Statistic groupBy value is invalid.",
                Map.of("value", value == null ? "" : value, "allowedValues", allowedValues)
        );
    }
}
