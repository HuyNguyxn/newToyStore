package com.example.new_toy_store.statistics.domain;

import com.example.new_toy_store.statistics.domain.exception.InvalidStatisticPeriodException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public record StatisticPeriod(
        LocalDate from,
        LocalDate to,
        ZoneId timezone,
        StatisticGroupBy requestedGroupBy,
        StatisticGroupBy appliedGroupBy,
        boolean compareWithPreviousPeriod
) {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int MAX_RANGE_DAYS = 731;

    public static StatisticPeriod of(
            LocalDate from,
            LocalDate to,
            String timezone,
            StatisticGroupBy groupBy,
            boolean compareWithPreviousPeriod
    ) {
        ZoneId zone = resolveZone(timezone);
        LocalDate resolvedTo = to == null ? LocalDate.now(zone) : to;
        LocalDate resolvedFrom = from == null ? resolvedTo.minusDays(29) : from;
        validateRange(resolvedFrom, resolvedTo, zone);

        StatisticGroupBy requested = groupBy == null ? StatisticGroupBy.AUTO : groupBy;
        StatisticGroupBy applied = resolveGroupBy(resolvedFrom, resolvedTo, requested);
        return new StatisticPeriod(resolvedFrom, resolvedTo, zone, requested, applied, compareWithPreviousPeriod);
    }

    public LocalDateTime startDateTime() {
        return from.atStartOfDay();
    }

    public LocalDateTime endExclusiveDateTime() {
        return to.plusDays(1).atStartOfDay();
    }

    public long dayCount() {
        return ChronoUnit.DAYS.between(from, to) + 1;
    }

    public boolean groupByAdjusted() {
        return requestedGroupBy != appliedGroupBy;
    }

    public StatisticPeriod previousPeriod() {
        long days = dayCount();
        LocalDate previousTo = from.minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(days - 1);
        return new StatisticPeriod(
                previousFrom,
                previousTo,
                timezone,
                requestedGroupBy,
                appliedGroupBy,
                false
        );
    }

    private static ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_ZONE;
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (DateTimeException ex) {
            throw InvalidStatisticPeriodException.invalidTimezone(timezone);
        }
    }

    private static void validateRange(LocalDate from, LocalDate to, ZoneId zone) {
        if (from.isAfter(to)) {
            throw InvalidStatisticPeriodException.fromDateAfterToDate(from, to);
        }
        if (to.isAfter(LocalDate.now(zone))) {
            throw InvalidStatisticPeriodException.futureDateNotAllowed(to);
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw InvalidStatisticPeriodException.dateRangeTooLarge(from, to, MAX_RANGE_DAYS);
        }
    }

    private static StatisticGroupBy resolveGroupBy(LocalDate from, LocalDate to, StatisticGroupBy requested) {
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        return requested.resolveFor(days);
    }
}
