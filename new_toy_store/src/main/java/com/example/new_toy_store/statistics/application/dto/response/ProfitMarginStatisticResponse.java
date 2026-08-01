package com.example.new_toy_store.statistics.application.dto.response;

public class ProfitMarginStatisticResponse {

    private final Integer productId;
    private final String productName;
    private final long soldQuantity;
    private final double revenue;
    private final double cost;
    private final double grossProfit;
    private final double marginPercent;

    public ProfitMarginStatisticResponse(Integer productId, String productName, long soldQuantity, double revenue, double cost) {
        this.productId = productId;
        this.productName = productName;
        this.soldQuantity = soldQuantity;
        this.revenue = round(revenue);
        this.cost = round(cost);
        this.grossProfit = round(revenue - cost);
        this.marginPercent = revenue <= 0 ? 0.0 : round(((revenue - cost) / revenue) * 100.0);
    }

    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public long getSoldQuantity() { return soldQuantity; }
    public double getRevenue() { return revenue; }
    public double getCost() { return cost; }
    public double getGrossProfit() { return grossProfit; }
    public double getMarginPercent() { return marginPercent; }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
