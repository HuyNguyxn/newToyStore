package com.example.new_toy_store.statistics.application.dto.response;

public class RevenueTrendPointResponse {

    private final String period;
    private final double grossRevenue;
    private final double refundAmount;
    private final double netRevenue;
    private final long createdOrderCount;
    private final long orderCount;
    private final long soldQuantity;
    private final long importedQuantity;
    private final double costOfGoodsSold;
    private final double importCost;
    private final double grossProfit;

    public RevenueTrendPointResponse(String period, double grossRevenue, double refundAmount, long orderCount) {
        this(period, grossRevenue, refundAmount, 0, orderCount, 0, 0, 0.0, 0.0);
    }

    public RevenueTrendPointResponse(String period, double grossRevenue, double refundAmount, long orderCount,
                                     long soldQuantity, long importedQuantity,
                                     double costOfGoodsSold, double importCost) {
        this(period, grossRevenue, refundAmount, 0, orderCount, soldQuantity, importedQuantity, costOfGoodsSold, importCost);
    }

    public RevenueTrendPointResponse(String period, double grossRevenue, double refundAmount,
                                     long createdOrderCount, long orderCount,
                                     long soldQuantity, long importedQuantity,
                                     double costOfGoodsSold, double importCost) {
        this.period = period;
        this.grossRevenue = round(grossRevenue);
        this.refundAmount = round(refundAmount);
        this.netRevenue = round(grossRevenue - refundAmount);
        this.createdOrderCount = createdOrderCount;
        this.orderCount = orderCount;
        this.soldQuantity = soldQuantity;
        this.importedQuantity = importedQuantity;
        this.costOfGoodsSold = round(costOfGoodsSold);
        this.importCost = round(importCost);
        this.grossProfit = round(this.netRevenue - this.costOfGoodsSold);
    }

    public String getPeriod() { return period; }
    public double getGrossRevenue() { return grossRevenue; }
    public double getRefundAmount() { return refundAmount; }
    public double getNetRevenue() { return netRevenue; }
    public long getCreatedOrderCount() { return createdOrderCount; }
    public long getOrderCount() { return orderCount; }
    public long getSoldQuantity() { return soldQuantity; }
    public long getImportedQuantity() { return importedQuantity; }
    public double getCostOfGoodsSold() { return costOfGoodsSold; }
    public double getImportCost() { return importCost; }
    public double getGrossProfit() { return grossProfit; }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
