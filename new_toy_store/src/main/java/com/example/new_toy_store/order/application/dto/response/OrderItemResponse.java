package com.example.new_toy_store.order.application.dto.response;

public class OrderItemResponse {

    private Integer id;
    private Integer productId;
    private Integer variantId;
    private String productName;
    private String variantAttributesSnapshot;
    private int quantity;
    private double price;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariantAttributesSnapshot() { return variantAttributesSnapshot; }
    public void setVariantAttributesSnapshot(String variantAttributesSnapshot) { this.variantAttributesSnapshot = variantAttributesSnapshot; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}