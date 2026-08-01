package com.example.new_toy_store.statistics.application.dto.response;

public class InventoryMovementStatisticResponse {

    private final String code;
    private final String label;
    private final String direction;
    private final long quantity;
    private final double amount;
    private final String description;

    public InventoryMovementStatisticResponse(
            String code,
            String label,
            String direction,
            long quantity,
            double amount,
            String description
    ) {
        this.code = code;
        this.label = label;
        this.direction = direction;
        this.quantity = quantity;
        this.amount = round(amount);
        this.description = description;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDirection() { return direction; }
    public long getQuantity() { return quantity; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
