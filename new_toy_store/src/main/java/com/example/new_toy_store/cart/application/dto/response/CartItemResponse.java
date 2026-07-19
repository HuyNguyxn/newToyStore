package com.example.new_toy_store.cart.application.dto.response;

public class CartItemResponse {

    private Integer id;
    private Integer productId;
    private Integer variantId;
    private String productName;
    private String variantAttributes;
    private String thumbnailUrl;
    private double addedPrice;
    private double originalPrice;
    private double finalPrice;
    private int quantity;
    private boolean isSelected;
    private boolean isAvailable;
    private boolean hasPriceChanged;
    private String message;

    public CartItemResponse(Integer id, Integer productId, Integer variantId, String productName,
                            String variantAttributes, String thumbnailUrl, double addedPrice, double originalPrice,
                            double finalPrice, int quantity, boolean isSelected, boolean isAvailable,
                            boolean hasPriceChanged, String message) {
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantAttributes = variantAttributes;
        this.thumbnailUrl = thumbnailUrl;
        this.addedPrice = addedPrice;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.quantity = quantity;
        this.isSelected = isSelected;
        this.isAvailable = isAvailable;
        this.hasPriceChanged = hasPriceChanged;
        this.message = message;
    }

    public Integer getId() { return id; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getVariantAttributes() { return variantAttributes; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public double getAddedPrice() { return addedPrice; }
    public double getOriginalPrice() { return originalPrice; }
    public double getFinalPrice() { return finalPrice; }
    public int getQuantity() { return quantity; }
    public boolean isSelected() { return isSelected; }
    public boolean isAvailable() { return isAvailable; }
    public boolean hasPriceChanged() { return hasPriceChanged; }
    public String getMessage() { return message; }
}