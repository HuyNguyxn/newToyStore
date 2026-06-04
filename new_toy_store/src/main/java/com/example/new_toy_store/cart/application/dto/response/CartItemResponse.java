package com.example.new_toy_store.cart.application.dto.response;

public class CartItemResponse {

    private Integer id;
    private Integer productId;
    private Integer variantId;
    private String productName;
    private String variantAttributes;
    private String thumbnailUrl;
    private double price;
    private int quantity;

    public CartItemResponse(Integer id, Integer productId, Integer variantId, String productName, String variantAttributes, String thumbnailUrl, double price, int quantity) {
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantAttributes = variantAttributes;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.quantity = quantity;
    }

    public Integer getId() { return id; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getVariantAttributes() { return variantAttributes; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
}