package com.example.new_toy_store.statistics.domain;

import com.example.new_toy_store.statistics.domain.exception.InvalidStatisticRequestException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatisticDateField {
    CREATED_AT("Created at", "Use the time when the record was created"),
    COMPLETED_AT("Completed at", "Use the time when an order reached completed status"),
    CANCELLED_AT("Cancelled at", "Use the time when an order reached cancelled status");

    private final String displayName;
    private final String description;

    StatisticDateField(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return name(); }
    public String getName() { return name(); }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StatisticDateField from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return CREATED_AT;
        }
        try {
            return StatisticDateField.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidStatisticRequestException("dateField", value);
        }
    }
}
