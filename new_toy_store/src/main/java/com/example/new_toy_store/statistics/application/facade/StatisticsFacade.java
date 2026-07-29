package com.example.new_toy_store.statistics.application.facade;

import com.example.new_toy_store.statistics.application.StatisticsService;
import com.example.new_toy_store.statistics.application.dto.response.StatisticsOverviewResponse;
import com.example.new_toy_store.statistics.application.dto.response.TopSellingProductResponse;
import com.example.new_toy_store.statistics.domain.StatisticPeriod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatisticsFacade {

    private final StatisticsService service;

    public StatisticsFacade(StatisticsService service) {
        this.service = service;
    }

    public StatisticsOverviewResponse getOverview(StatisticPeriod period, int topLimit, int lowStockThreshold) {
        return service.getOverview(period, topLimit, lowStockThreshold);
    }

    public List<TopSellingProductResponse> getTopSellingProducts(StatisticPeriod period, int limit) {
        return service.getTopSellingProducts(period, limit);
    }
}
