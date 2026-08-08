package com.example.new_toy_store.statistics.application.dto.request;

import com.example.new_toy_store.statistics.domain.StatisticGroupBy;
import com.example.new_toy_store.statistics.domain.StatisticDateField;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Query payload for the statistics dashboard. Bound from URL query parameters. */
public class StatisticsOverviewRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    @NotBlank(message = "timezone must not be blank")
    private String timezone = "Asia/Ho_Chi_Minh";

    private StatisticGroupBy groupBy = StatisticGroupBy.AUTO;
    private StatisticDateField dateField = StatisticDateField.CREATED_AT;
    private boolean compareWithPreviousPeriod;
    private Boolean includeTestOrders = false;

    @Min(1)
    @Max(20)
    private int topLimit = 5;

    @Min(0)
    private int lowStockThreshold = 5;

    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }
    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public StatisticGroupBy getGroupBy() { return groupBy; }
    public void setGroupBy(StatisticGroupBy groupBy) { this.groupBy = groupBy; }
    public StatisticDateField getDateField() { return dateField; }
    public void setDateField(StatisticDateField dateField) { this.dateField = dateField; }
    public boolean isCompareWithPreviousPeriod() { return compareWithPreviousPeriod; }
    public void setCompareWithPreviousPeriod(boolean compareWithPreviousPeriod) { this.compareWithPreviousPeriod = compareWithPreviousPeriod; }
    public Boolean getIncludeTestOrders() { return includeTestOrders; }
    public void setIncludeTestOrders(Boolean includeTestOrders) { this.includeTestOrders = includeTestOrders; }
    public int getTopLimit() { return topLimit; }
    public void setTopLimit(int topLimit) { this.topLimit = topLimit; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
}
