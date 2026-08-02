package com.example.new_toy_store.product.application.dto.response;

import java.util.List;

public class ProductResponse {

    private Integer id;
    private String name;
    private double basePrice;
    private String status;
    private Integer supplierId;
    private String supplierName;

    private List<Integer> categoryIds;
    private String thumbnailUrl;
    private List<ProductImageResponse> images;
    private double averageRating;
    private int reviewCount;
    private List<ProductVariantResponse> variants;
    private ProductEnumOptionResponse statusDetail;
    private List<ProductEnumOptionResponse> allowedNextStatuses;
    private List<String> allowedActions;
    private boolean purchasable;
    private boolean quickAddAvailable;
    private Integer defaultVariantId;
    private int defaultVariantStockQuantity;

    public ProductResponse(Integer id, String name, double basePrice, String status, Integer supplierId,
                           List<Integer> categoryIds, String thumbnailUrl, List<ProductImageResponse> images,
                           double averageRating, int reviewCount,
                           List<ProductVariantResponse> variants, ProductEnumOptionResponse statusDetail,
                           List<ProductEnumOptionResponse> allowedNextStatuses, List<String> allowedActions,
                           boolean purchasable, boolean quickAddAvailable,
                           Integer defaultVariantId, int defaultVariantStockQuantity) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.status = status;
        this.supplierId = supplierId;
        this.categoryIds = categoryIds;
        this.thumbnailUrl = thumbnailUrl;
        this.images = images == null ? List.of() : List.copyOf(images);
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.variants = variants;
        this.statusDetail = statusDetail;
        this.allowedNextStatuses = allowedNextStatuses == null ? List.of() : List.copyOf(allowedNextStatuses);
        this.allowedActions = allowedActions == null ? List.of() : List.copyOf(allowedActions);
        this.purchasable = purchasable;
        this.quickAddAvailable = quickAddAvailable;
        this.defaultVariantId = defaultVariantId;
        this.defaultVariantStockQuantity = defaultVariantStockQuantity;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public String getStatus() { return status; }
    public Integer getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public List<Integer> getCategoryIds() { return categoryIds; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public List<ProductImageResponse> getImages() { return images; }
    public double getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
    public List<ProductVariantResponse> getVariants() { return variants; }
    public ProductEnumOptionResponse getStatusDetail() { return statusDetail; }
    public List<ProductEnumOptionResponse> getAllowedNextStatuses() { return allowedNextStatuses; }
    public List<String> getAllowedActions() { return allowedActions; }
    public boolean isPurchasable() { return purchasable; }
    public boolean isQuickAddAvailable() { return quickAddAvailable; }
    public Integer getDefaultVariantId() { return defaultVariantId; }
    public int getDefaultVariantStockQuantity() { return defaultVariantStockQuantity; }
}
