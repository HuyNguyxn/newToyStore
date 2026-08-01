package com.example.new_toy_store.statistics.application.dto.response;

public class BreakdownStatisticResponse {

    private final String code;
    private final String label;
    private final long count;
    private final double amount;
    private final double sharePercent;

    public BreakdownStatisticResponse(String code, String label, long count, double amount, double totalAmount) {
        this.code = code;
        this.label = label;
        this.count = count;
        this.amount = round(amount);
        this.sharePercent = totalAmount <= 0 ? 0.0 : round((amount / totalAmount) * 100.0);
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public long getCount() { return count; }
    public double getAmount() { return amount; }
    public double getSharePercent() { return sharePercent; }

    private static double round(double value) {
        return Math.max(0.0, Math.round(value * 100.0) / 100.0);
    }
}
