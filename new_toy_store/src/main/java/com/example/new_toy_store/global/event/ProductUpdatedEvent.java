package com.example.new_toy_store.global.event;

public class ProductUpdatedEvent {
    private Integer productId;
    private Integer variantId;
    private double newPrice;

    public ProductUpdatedEvent(Integer productId, Integer variantId, double newPrice) {
        this.productId = productId;
        this.variantId = variantId;
        this.newPrice = newPrice;
    }

    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public double getNewPrice() { return newPrice; }
}