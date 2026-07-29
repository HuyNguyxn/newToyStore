package com.example.new_toy_store.statistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Map;

public class InvalidStatisticPeriodException extends StatisticsDomainException {

    private InvalidStatisticPeriodException(String errorCode, String message, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, errorCode, message, contextData);
    }

    public static InvalidStatisticPeriodException fromDateAfterToDate(LocalDate from, LocalDate to) {
        return new InvalidStatisticPeriodException(
                "FROM_DATE_AFTER_TO_DATE",
                "From date must not be after to date.",
                Map.of("from", safe(from), "to", safe(to))
        );
    }

    public static InvalidStatisticPeriodException futureDateNotAllowed(LocalDate to) {
        return new InvalidStatisticPeriodException(
                "FUTURE_DATE_NOT_ALLOWED",
                "To date must not be in the future.",
                Map.of("to", safe(to))
        );
    }

    public static InvalidStatisticPeriodException dateRangeTooLarge(LocalDate from, LocalDate to, int maxDays) {
        return new InvalidStatisticPeriodException(
                "DATE_RANGE_TOO_LARGE",
                "Statistic period is too large.",
                Map.of("from", safe(from), "to", safe(to), "maxDays", maxDays)
        );
    }

    public static InvalidStatisticPeriodException invalidTimezone(String timezone) {
        return new InvalidStatisticPeriodException(
                "INVALID_TIMEZONE",
                "Timezone is invalid. Please use a valid IANA timezone such as Asia/Ho_Chi_Minh.",
                Map.of("timezone", timezone == null ? "" : timezone)
        );
    }

    private static String safe(LocalDate value) {
        return value == null ? "" : value.toString();
    }
}
