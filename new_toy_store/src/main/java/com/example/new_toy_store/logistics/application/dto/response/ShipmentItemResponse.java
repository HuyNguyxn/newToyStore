package com.example.new_toy_store.logistics.application.dto.response;

public class ShipmentItemResponse {

    private Integer id;
    private Integer productId;
    private Integer variantId;
    private String productNameSnapshot;
    private String variantSnapshot;
    private int quantity;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }
    public String getVariantSnapshot() { return variantSnapshot; }
    public void setVariantSnapshot(String variantSnapshot) { this.variantSnapshot = variantSnapshot; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
