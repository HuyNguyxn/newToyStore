package com.example.new_toy_store.order.application.dto.response;

public class OrderLogisticsItemSnapshot {

    private final Integer productId;
    private final Integer variantId;
    private final String productName;
    private final String variantSnapshot;
    private final int quantity;

    public OrderLogisticsItemSnapshot(
            Integer productId,
            Integer variantId,
            String productName,
            String variantSnapshot,
            int quantity
    ) {
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantSnapshot = variantSnapshot;
        this.quantity = quantity;
    }

    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getVariantSnapshot() { return variantSnapshot; }
    public int getQuantity() { return quantity; }
}
