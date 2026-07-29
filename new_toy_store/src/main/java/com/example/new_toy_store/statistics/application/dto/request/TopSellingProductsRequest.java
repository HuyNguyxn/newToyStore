package com.example.new_toy_store.statistics.application.dto.request;

import com.example.new_toy_store.statistics.domain.StatisticGroupBy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Query payload for the top-selling products report. Bound from URL query parameters. */
public class TopSellingProductsRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    @NotBlank(message = "timezone must not be blank")
    private String timezone = "Asia/Ho_Chi_Minh";

    private StatisticGroupBy groupBy = StatisticGroupBy.AUTO;

    @Min(1)
    @Max(50)
    private int limit = 10;

    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }
    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public StatisticGroupBy getGroupBy() { return groupBy; }
    public void setGroupBy(StatisticGroupBy groupBy) { this.groupBy = groupBy; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
