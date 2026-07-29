package com.example.new_toy_store.statistics.application.dto.response;

public class PaymentMethodStatisticResponse {

    private final String method;
    private final long transactionCount;
    private final double amount;
    private final double sharePercent;

    public PaymentMethodStatisticResponse(String method, long transactionCount, double amount, double totalAmount) {
        this.method = method;
        this.transactionCount = transactionCount;
        this.amount = round(amount);
        this.sharePercent = totalAmount <= 0 ? 0.0 : round((amount / totalAmount) * 100.0);
    }

    public String getMethod() { return method; }
    public long getTransactionCount() { return transactionCount; }
    public double getAmount() { return amount; }
    public double getSharePercent() { return sharePercent; }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
