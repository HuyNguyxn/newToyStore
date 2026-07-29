package com.example.new_toy_store.statistics.application.dto.response;

public class RevenueTrendPointResponse {

    private final String period;
    private final double grossRevenue;
    private final double refundAmount;
    private final double netRevenue;
    private final long orderCount;

    public RevenueTrendPointResponse(String period, double grossRevenue, double refundAmount, long orderCount) {
        this.period = period;
        this.grossRevenue = round(grossRevenue);
        this.refundAmount = round(refundAmount);
        this.netRevenue = round(grossRevenue - refundAmount);
        this.orderCount = orderCount;
    }

    public String getPeriod() { return period; }
    public double getGrossRevenue() { return grossRevenue; }
    public double getRefundAmount() { return refundAmount; }
    public double getNetRevenue() { return netRevenue; }
    public long getOrderCount() { return orderCount; }

    private static double round(double value) {
        return Math.max(0.0, Math.round(value * 100.0) / 100.0);
    }
}
