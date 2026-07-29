package com.example.new_toy_store.statistics.application.dto.response;

public class InventoryStatisticResponse {

    private final long stockQuantity;
    private final long reservedQuantity;
    private final long availableQuantity;
    private final long lowStockVariantCount;

    public InventoryStatisticResponse(long stockQuantity, long reservedQuantity, long lowStockVariantCount) {
        this.stockQuantity = stockQuantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = Math.max(0, stockQuantity - reservedQuantity);
        this.lowStockVariantCount = lowStockVariantCount;
    }

    public long getStockQuantity() { return stockQuantity; }
    public long getReservedQuantity() { return reservedQuantity; }
    public long getAvailableQuantity() { return availableQuantity; }
    public long getLowStockVariantCount() { return lowStockVariantCount; }
}
