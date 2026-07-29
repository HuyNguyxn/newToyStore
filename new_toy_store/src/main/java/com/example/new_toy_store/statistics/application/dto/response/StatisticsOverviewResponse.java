package com.example.new_toy_store.statistics.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class StatisticsOverviewResponse {

    private final LocalDateTime generatedAt;
    private final StatisticPeriodResponse period;
    private final List<KpiMetricResponse> kpis;
    private final List<RevenueTrendPointResponse> revenueTrend;
    private final List<StatusCountResponse> orderStatus;
    private final List<StatusCountResponse> paymentStatus;
    private final List<StatusCountResponse> refundStatus;
    private final List<StatusCountResponse> shipmentStatus;
    private final List<StatusCountResponse> userStatus;
    private final List<StatusCountResponse> productStatus;
    private final List<PaymentMethodStatisticResponse> paymentMethods;
    private final InventoryStatisticResponse inventory;
    private final List<TopSellingProductResponse> topSellingProducts;

    public StatisticsOverviewResponse(
            LocalDateTime generatedAt,
            StatisticPeriodResponse period,
            List<KpiMetricResponse> kpis,
            List<RevenueTrendPointResponse> revenueTrend,
            List<StatusCountResponse> orderStatus,
            List<StatusCountResponse> paymentStatus,
            List<StatusCountResponse> refundStatus,
            List<StatusCountResponse> shipmentStatus,
            List<StatusCountResponse> userStatus,
            List<StatusCountResponse> productStatus,
            List<PaymentMethodStatisticResponse> paymentMethods,
            InventoryStatisticResponse inventory,
            List<TopSellingProductResponse> topSellingProducts
    ) {
        this.generatedAt = generatedAt;
        this.period = period;
        this.kpis = kpis;
        this.revenueTrend = revenueTrend;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.refundStatus = refundStatus;
        this.shipmentStatus = shipmentStatus;
        this.userStatus = userStatus;
        this.productStatus = productStatus;
        this.paymentMethods = paymentMethods;
        this.inventory = inventory;
        this.topSellingProducts = topSellingProducts;
    }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public StatisticPeriodResponse getPeriod() { return period; }
    public List<KpiMetricResponse> getKpis() { return kpis; }
    public List<RevenueTrendPointResponse> getRevenueTrend() { return revenueTrend; }
    public List<StatusCountResponse> getOrderStatus() { return orderStatus; }
    public List<StatusCountResponse> getPaymentStatus() { return paymentStatus; }
    public List<StatusCountResponse> getRefundStatus() { return refundStatus; }
    public List<StatusCountResponse> getShipmentStatus() { return shipmentStatus; }
    public List<StatusCountResponse> getUserStatus() { return userStatus; }
    public List<StatusCountResponse> getProductStatus() { return productStatus; }
    public List<PaymentMethodStatisticResponse> getPaymentMethods() { return paymentMethods; }
    public InventoryStatisticResponse getInventory() { return inventory; }
    public List<TopSellingProductResponse> getTopSellingProducts() { return topSellingProducts; }
}
