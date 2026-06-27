package com.example.new_toy_store.cart.application.dto.response;

public class CartItemResponse {

    private Integer id;
    private Integer productId;
    private Integer variantId;
    private String productName;
    private String variantAttributes;
    private String thumbnailUrl;

    private double originalPrice;
    private double finalPrice;

    private int quantity;
    private boolean isAvailable;
    private String message;

    public CartItemResponse(Integer id, Integer productId, Integer variantId, String productName,
                            String variantAttributes, String thumbnailUrl, double originalPrice,
                            double finalPrice, int quantity, boolean isAvailable, String message) {
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantAttributes = variantAttributes;
        this.thumbnailUrl = thumbnailUrl;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.quantity = quantity;
        this.isAvailable = isAvailable;
        this.message = message;
    }

    public Integer getId() { return id; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getVariantAttributes() { return variantAttributes; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public double getOriginalPrice() { return originalPrice; }
    public double getFinalPrice() { return finalPrice; }
    public int getQuantity() { return quantity; }
    public boolean isAvailable() { return isAvailable; }
    public String getMessage() { return message; }
}