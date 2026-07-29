package com.example.new_toy_store.statistics.api;

import com.example.new_toy_store.statistics.application.facade.StatisticsFacade;
import com.example.new_toy_store.statistics.application.dto.response.StatisticsOverviewResponse;
import com.example.new_toy_store.statistics.application.dto.response.TopSellingProductResponse;
import com.example.new_toy_store.statistics.domain.StatisticGroupBy;
import com.example.new_toy_store.statistics.domain.StatisticPeriod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/statistics")
@Validated
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class StatisticsController {

    private final StatisticsFacade facade;

    public StatisticsController(StatisticsFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/overview")
    public StatisticsOverviewResponse getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone,
            @RequestParam(defaultValue = "AUTO") StatisticGroupBy groupBy,
            @RequestParam(defaultValue = "false") boolean compareWithPreviousPeriod,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int topLimit,
            @RequestParam(defaultValue = "5") @Min(0) int lowStockThreshold
    ) {
        StatisticPeriod period = StatisticPeriod.of(from, to, timezone, groupBy, compareWithPreviousPeriod);
        return facade.getOverview(period, topLimit, lowStockThreshold);
    }

    @GetMapping("/products/top-selling")
    public List<TopSellingProductResponse> getTopSellingProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone,
            @RequestParam(defaultValue = "AUTO") StatisticGroupBy groupBy,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        StatisticPeriod period = StatisticPeriod.of(from, to, timezone, groupBy, false);
        return facade.getTopSellingProducts(period, limit);
    }
}
