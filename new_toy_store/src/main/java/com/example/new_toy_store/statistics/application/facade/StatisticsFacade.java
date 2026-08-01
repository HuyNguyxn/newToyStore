package com.example.new_toy_store.statistics.application.facade;

import com.example.new_toy_store.statistics.application.StatisticsService;
import com.example.new_toy_store.statistics.application.dto.response.BreakdownStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.PaymentMethodStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.ProfitMarginStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.RevenueTrendPointResponse;
import com.example.new_toy_store.statistics.application.dto.response.StatisticsOverviewResponse;
import com.example.new_toy_store.statistics.application.dto.response.TopSellingProductResponse;
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

    public List<RevenueTrendPointResponse> getRevenueTrend(StatisticPeriod period) {
        return service.getRevenueTrend(period);
    }

    public List<PaymentMethodStatisticResponse> getPaymentMethods(StatisticPeriod period) {
        return service.getPaymentMethods(period);
    }

    public List<BreakdownStatisticResponse> getRevenueByCategory(StatisticPeriod period, int limit) {
        return service.getRevenueByCategory(period, limit);
    }

    public List<BreakdownStatisticResponse> getRevenueByPromotion(StatisticPeriod period, int limit) {
        return service.getRevenueByPromotion(period, limit);
    }

    public List<BreakdownStatisticResponse> getTopSpendingCustomers(StatisticPeriod period, int limit) {
        return service.getTopSpendingCustomers(period, limit);
    }

    public List<BreakdownStatisticResponse> getPaymentFailureReasons(StatisticPeriod period, int limit) {
        return service.getPaymentFailureReasons(period, limit);
    }

    public List<BreakdownStatisticResponse> getRefundReasons(StatisticPeriod period, int limit) {
        return service.getRefundReasons(period, limit);
    }

    public List<BreakdownStatisticResponse> getShipmentsByProvider(StatisticPeriod period) {
        return service.getShipmentsByProvider(period);
    }

    public List<BreakdownStatisticResponse> getShipmentFailureReasons(StatisticPeriod period, int limit) {
        return service.getShipmentFailureReasons(period, limit);
    }

    public List<BreakdownStatisticResponse> getCustomerSummary(StatisticPeriod period) {
        return service.getCustomerSummary(period);
    }

    public List<BreakdownStatisticResponse> getCustomerTrend(StatisticPeriod period) {
        return service.getCustomerTrend(period);
    }

    public List<BreakdownStatisticResponse> getRefundByProduct(StatisticPeriod period, int limit) {
        return service.getRefundByProduct(period, limit);
    }

    public List<BreakdownStatisticResponse> getShipmentsByRegion(StatisticPeriod period, int limit) {
        return service.getShipmentsByRegion(period, limit);
    }

    public List<BreakdownStatisticResponse> getInventoryMovements(StatisticPeriod period) {
        return service.getInventoryMovements(period);
    }

    public List<ProfitMarginStatisticResponse> getProfitMargin(StatisticPeriod period, int limit) {
        return service.getProfitMargin(period, limit);
    }
}
