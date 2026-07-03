package com.example.new_toy_store.product.application.dto.request;

public class ImportedStockRequest {
    private Integer variantId;
    private int quantity;
    private double importPrice;

    public ImportedStockRequest(Integer variantId, int quantity, double importPrice) {
        this.variantId = variantId;
        this.quantity = quantity;
        this.importPrice = importPrice;
    }

    public Integer getVariantId() {
        return variantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getImportPrice() {
        return importPrice;
    }
}