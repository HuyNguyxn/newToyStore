package com.example.new_toy_store.statistics.domain;

import com.example.new_toy_store.statistics.domain.exception.InvalidStatisticGroupByException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatisticGroupBy {

    AUTO("Auto", "System chooses the best grouping for the selected period"),
    DAY("Day", "Group statistics by day"),
    WEEK("Week", "Group statistics by ISO week"),
    MONTH("Month", "Group statistics by month"),
    QUARTER("Quarter", "Group statistics by quarter"),
    YEAR("Year", "Group statistics by year");

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
