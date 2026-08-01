package com.example.new_toy_store.statistics.application.dto.response;

public class ProfitMarginStatisticResponse {

    private final Integer productId;
    private final String productName;
    private final long soldQuantity;
    private final double revenue;
    private final double refundAmount;
    private final double netRevenue;
    private final double cost;
    private final double grossProfit;
    private final double netProfit;
    private final double marginPercent;

    public ProfitMarginStatisticResponse(
            Integer productId,
            String productName,
            long soldQuantity,
            double revenue,
            double refundAmount,
            double cost
    ) {
        double roundedRevenue = round(revenue);
        double roundedRefundAmount = round(refundAmount);
        double roundedNetRevenue = round(Math.max(0.0, revenue - refundAmount));
        double roundedCost = round(cost);
        double roundedGrossProfit = round(revenue - cost);
        double roundedNetProfit = round(roundedNetRevenue - cost);

        this.productId = productId;
        this.productName = productName;
        this.soldQuantity = soldQuantity;
        this.revenue = roundedRevenue;
        this.refundAmount = roundedRefundAmount;
        this.netRevenue = roundedNetRevenue;
        this.cost = roundedCost;
        this.grossProfit = roundedGrossProfit;
        this.netProfit = roundedNetProfit;
        this.marginPercent = roundedNetRevenue <= 0 ? 0.0 : round((roundedNetProfit / roundedNetRevenue) * 100.0);
    }

    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public long getSoldQuantity() { return soldQuantity; }
    public double getRevenue() { return revenue; }
    public double getRefundAmount() { return refundAmount; }
    public double getNetRevenue() { return netRevenue; }
    public double getCost() { return cost; }
    public double getGrossProfit() { return grossProfit; }
    public double getNetProfit() { return netProfit; }
    public double getMarginPercent() { return marginPercent; }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
