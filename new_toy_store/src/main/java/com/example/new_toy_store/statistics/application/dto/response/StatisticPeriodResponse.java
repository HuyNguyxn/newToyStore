package com.example.new_toy_store.statistics.application.dto.response;

import com.example.new_toy_store.statistics.domain.StatisticPeriod;

import java.time.LocalDate;

public class StatisticPeriodResponse {

    private final LocalDate from;
    private final LocalDate to;
    private final String timezone;
    private final String requestedGroupBy;
    private final String appliedGroupBy;
    private final boolean groupByAdjusted;
    private final boolean compareWithPreviousPeriod;

    public StatisticPeriodResponse(StatisticPeriod period) {
        this.from = period.from();
        this.to = period.to();
        this.timezone = period.timezone().getId();
        this.requestedGroupBy = period.requestedGroupBy().name();
        this.appliedGroupBy = period.appliedGroupBy().name();
        this.groupByAdjusted = period.groupByAdjusted();
        this.compareWithPreviousPeriod = period.compareWithPreviousPeriod();
    }

    public LocalDate getFrom() { return from; }
    public LocalDate getTo() { return to; }
    public String getTimezone() { return timezone; }
    public String getRequestedGroupBy() { return requestedGroupBy; }
    public String getAppliedGroupBy() { return appliedGroupBy; }
    public boolean isGroupByAdjusted() { return groupByAdjusted; }
    public boolean isCompareWithPreviousPeriod() { return compareWithPreviousPeriod; }
}
