package com.example.new_toy_store.statistics.application.dto.response;

public class TopSpendingCustomerResponse {

    private final Integer userId;
    private final String customerName;
    private final long orderCount;
    private final long purchasedQuantity;
    private final double averageProductsPerOrder;
    private final double totalSpent;
    private final double sharePercent;

    public TopSpendingCustomerResponse(Integer userId,
                                       String customerName,
                                       long orderCount,
                                       long purchasedQuantity,
                                       double totalSpent,
                                       double totalRevenue) {
        this.userId = userId;
        this.customerName = customerName;
        this.orderCount = orderCount;
        this.purchasedQuantity = purchasedQuantity;
        this.averageProductsPerOrder = orderCount == 0 ? 0.0 : round((double) purchasedQuantity / orderCount);
        this.totalSpent = round(totalSpent);
        this.sharePercent = totalRevenue <= 0 ? 0.0 : round((totalSpent / totalRevenue) * 100.0);
    }

    public Integer getUserId() { return userId; }
    public String getCustomerName() { return customerName; }
    public long getOrderCount() { return orderCount; }
    public long getPurchasedQuantity() { return purchasedQuantity; }
    public double getAverageProductsPerOrder() { return averageProductsPerOrder; }
    public double getTotalSpent() { return totalSpent; }
    public double getSharePercent() { return sharePercent; }

    private static double round(double value) {
        return Math.max(0.0, Math.round(value * 100.0) / 100.0);
    }
}
