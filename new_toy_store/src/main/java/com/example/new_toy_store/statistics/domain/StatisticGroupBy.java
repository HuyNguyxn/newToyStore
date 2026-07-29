package com.example.new_toy_store.statistics.domain;

import com.example.new_toy_store.statistics.domain.exception.InvalidStatisticGroupByException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatisticGroupBy {

    AUTO("Auto", "System chooses the best grouping for the selected period") {
        @Override
        public StatisticGroupBy resolveFor(long dayCount) {
            if (dayCount <= 31) return DAY;
            if (dayCount <= 120) return WEEK;
            return MONTH;
        }

        @Override
        public LocalDate bucketStart(LocalDate date) {
            return resolveFor(1).bucketStart(date);
        }

        @Override
        public LocalDate nextBucket(LocalDate date) {
            return resolveFor(1).nextBucket(date);
        }

        @Override
        public String bucketKey(LocalDate date) {
            return resolveFor(1).bucketKey(date);
        }
    },
    DAY("Day", "Group statistics by day") {
        @Override
        public StatisticGroupBy resolveFor(long dayCount) {
            return dayCount > 366 ? MONTH : DAY;
        }

        @Override
        public LocalDate bucketStart(LocalDate date) {
            return date;
        }

        @Override
        public LocalDate nextBucket(LocalDate date) {
            return date.plusDays(1);
        }

        @Override
        public String bucketKey(LocalDate date) {
            return date.toString();
        }
    },
    WEEK("Week", "Group statistics by ISO week") {
        @Override
        public StatisticGroupBy resolveFor(long dayCount) {
            return WEEK;
        }

        @Override
        public LocalDate bucketStart(LocalDate date) {
            return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        @Override
        public LocalDate nextBucket(LocalDate date) {
            return date.plusWeeks(1);
        }

        @Override
        public String bucketKey(LocalDate date) {
            LocalDate start = bucketStart(date);
            return start.getYear() + "-W" + String.format("%02d", start.get(WeekFields.ISO.weekOfWeekBasedYear()));
        }
    },
    MONTH("Month", "Group statistics by month") {
        @Override
        public StatisticGroupBy resolveFor(long dayCount) {
            return MONTH;
        }

        @Override
        public LocalDate bucketStart(LocalDate date) {
            return date.withDayOfMonth(1);
        }

        @Override
        public LocalDate nextBucket(LocalDate date) {
            return date.plusMonths(1);
        }

        @Override
        public String bucketKey(LocalDate date) {
            LocalDate start = bucketStart(date);
            return start.getYear() + "-" + String.format("%02d", start.getMonthValue());
        }
    },
    QUARTER("Quarter", "Group statistics by quarter") {
        @Override
        public StatisticGroupBy resolveFor(long dayCount) {
            return QUARTER;
        }

        @Override
        public LocalDate bucketStart(LocalDate date) {
            return LocalDate.of(date.getYear(), (((date.getMonthValue() - 1) / 3) * 3) + 1, 1);
        }

        @Override
        public LocalDate nextBucket(LocalDate date) {
            return date.plusMonths(3);
        }

        @Override
        public String bucketKey(LocalDate date) {
            LocalDate start = bucketStart(date);
            return start.getYear() + "-Q" + (((start.getMonthValue() - 1) / 3) + 1);
        }
    },
    YEAR("Year", "Group statistics by year") {
        @Override
        public StatisticGroupBy resolveFor(long dayCount) {
            return YEAR;
        }

        @Override
        public LocalDate bucketStart(LocalDate date) {
            return LocalDate.of(date.getYear(), 1, 1);
        }

        @Override
        public LocalDate nextBucket(LocalDate date) {
            return date.plusYears(1);
        }

        @Override
        public String bucketKey(LocalDate date) {
            return String.valueOf(date.getYear());
        }
    };

    private final String displayName;
    private final String description;

    StatisticGroupBy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getName() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public abstract StatisticGroupBy resolveFor(long dayCount);

    public abstract LocalDate bucketStart(LocalDate date);

    public abstract LocalDate nextBucket(LocalDate date);

    public abstract String bucketKey(LocalDate date);

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StatisticGroupBy from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return AUTO;
        }
        try {
            return StatisticGroupBy.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidStatisticGroupByException(
                    value,
                    Arrays.stream(StatisticGroupBy.values()).map(Enum::name).toList()
            );
        }
    }
}
