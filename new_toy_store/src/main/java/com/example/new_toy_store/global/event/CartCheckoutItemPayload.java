package com.example.new_toy_store.global.event;

public final class CartCheckoutItemPayload {
    private final Integer productId;
    private final Integer variantId;
    private final String productName;
    private final String variantAttributesSnapshot;
    private final int quantity;
    private final double price;

    public CartCheckoutItemPayload(Integer productId, Integer variantId, String productName,
                                   String variantAttributesSnapshot, int quantity, double price) {
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantAttributesSnapshot = variantAttributesSnapshot;
        this.quantity = quantity;
        this.price = price;
    }

    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getVariantAttributesSnapshot() { return variantAttributesSnapshot; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}
