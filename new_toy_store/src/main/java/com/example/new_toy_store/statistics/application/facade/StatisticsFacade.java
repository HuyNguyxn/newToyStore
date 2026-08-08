package com.example.new_toy_store.statistics.application.facade;

import com.example.new_toy_store.statistics.application.StatisticsService;
import com.example.new_toy_store.statistics.application.dto.response.BreakdownStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.PaymentMethodStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.RevenueTrendPointResponse;
import com.example.new_toy_store.statistics.application.dto.response.StatisticsOverviewResponse;
import com.example.new_toy_store.statistics.application.dto.response.TopSellingProductResponse;
import com.example.new_toy_store.statistics.application.dto.response.InventoryMovementStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.ProfitMarginStatisticResponse;
import com.example.new_toy_store.statistics.domain.StatisticDateField;
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

    public StatisticsOverviewResponse getOverview(StatisticPeriod period, int topLimit, int lowStockThreshold, StatisticDateField dateField) {
        return service.getOverview(period, topLimit, lowStockThreshold, dateField);
    }

    public List<TopSellingProductResponse> getTopSellingProducts(StatisticPeriod period, int limit) {
        return service.getTopSellingProducts(period, limit);
    }

    public List<TopSellingProductResponse> getSlowSellingProducts(StatisticPeriod period, int limit, int maxUnits) {
        return service.getSlowSellingProducts(period, limit, maxUnits);
    }

    public List<RevenueTrendPointResponse> getRevenueTrend(StatisticPeriod period) {
        return service.getRevenueTrend(period);
    }

    public List<PaymentMethodStatisticResponse> getPaymentMethods(StatisticPeriod period) {
        return service.getPaymentMethods(period);
    }

    public List<BreakdownStatisticResponse> getRevenueByCategory(StatisticPeriod period, int limit) {
        return service.getRevenueByCategory(period, limit);
    }

    public List<BreakdownStatisticResponse> getTopSpendingCustomers(StatisticPeriod period, int limit) {
        return service.getTopSpendingCustomers(period, limit);
    }

    public List<InventoryMovementStatisticResponse> getInventorySnapshot(int lowStockThreshold) {
        return service.getInventorySnapshot(lowStockThreshold);
    }

    public List<InventoryMovementStatisticResponse> getInventoryMovements(StatisticPeriod period, int lowStockThreshold) {
        return service.getInventoryMovements(period, lowStockThreshold);
    }

    public List<ProfitMarginStatisticResponse> getProfitMargin(StatisticPeriod period, int limit) {
        return service.getProfitMargin(period, limit);
    }
}
