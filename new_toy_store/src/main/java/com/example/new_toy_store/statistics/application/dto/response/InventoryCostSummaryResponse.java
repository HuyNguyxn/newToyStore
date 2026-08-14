package com.example.new_toy_store.statistics.application.dto.response;

public class InventoryCostSummaryResponse {

    private final Integer variantId;
    private final int variantCount;
    private final double currentSellingPrice;
    private final double currentMac;
    private final double latestImportPrice;
    private final long stockQuantity;
    private final double grossMarginPercent;
    private final boolean hasStock;
    private final boolean hasCompletedImport;

    public InventoryCostSummaryResponse(
            Integer variantId,
            int variantCount,
            double currentSellingPrice,
            double currentMac,
            double latestImportPrice,
            long stockQuantity,
            double grossMarginPercent,
            boolean hasCompletedImport
    ) {
        this.variantId = variantId;
        this.variantCount = variantCount;
        this.currentSellingPrice = currentSellingPrice;
        this.currentMac = currentMac;
        this.latestImportPrice = latestImportPrice;
        this.stockQuantity = stockQuantity;
        this.grossMarginPercent = grossMarginPercent;
        this.hasStock = stockQuantity > 0;
        this.hasCompletedImport = hasCompletedImport;
    }

    public Integer getVariantId() { return variantId; }
    public int getVariantCount() { return variantCount; }
    public double getCurrentSellingPrice() { return currentSellingPrice; }
    public double getCurrentMac() { return currentMac; }
    public double getLatestImportPrice() { return latestImportPrice; }
    public long getStockQuantity() { return stockQuantity; }
    public double getGrossMarginPercent() { return grossMarginPercent; }
    public boolean isHasStock() { return hasStock; }
    public boolean isHasCompletedImport() { return hasCompletedImport; }
}
