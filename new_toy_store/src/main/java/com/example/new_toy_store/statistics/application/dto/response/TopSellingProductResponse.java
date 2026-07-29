package com.example.new_toy_store.statistics.application.dto.response;

public class TopSellingProductResponse {

    private final Integer productId;
    private final String productName;
    private final long soldQuantity;
    private final long orderCount;
    private final double grossRevenue;

    public TopSellingProductResponse(Integer productId, String productName, long soldQuantity, long orderCount, double grossRevenue) {
        this.productId = productId;
        this.productName = productName;
        this.soldQuantity = soldQuantity;
        this.orderCount = orderCount;
        this.grossRevenue = Math.max(0.0, Math.round(grossRevenue * 100.0) / 100.0);
    }

    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public long getSoldQuantity() { return soldQuantity; }
    public long getOrderCount() { return orderCount; }
    public double getGrossRevenue() { return grossRevenue; }
}
