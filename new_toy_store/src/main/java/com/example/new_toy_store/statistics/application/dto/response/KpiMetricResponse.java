package com.example.new_toy_store.statistics.application.dto.response;

public class KpiMetricResponse {

    private final String code;
    private final String label;
    private final double value;
    private final Double previousValue;
    private final Double changePercent;

    public KpiMetricResponse(String code, String label, double value, Double previousValue) {
        this.code = code;
        this.label = label;
        this.value = round(value);
        this.previousValue = previousValue == null ? null : round(previousValue);
        this.changePercent = previousValue == null ? null : calculateChangePercent(value, previousValue);
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public double getValue() { return value; }
    public Double getPreviousValue() { return previousValue; }
    public Double getChangePercent() { return changePercent; }

    private static Double calculateChangePercent(double value, double previousValue) {
        if (previousValue == 0) {
            return value == 0 ? 0.0 : 100.0;
        }
        return round(((value - previousValue) / previousValue) * 100.0);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
