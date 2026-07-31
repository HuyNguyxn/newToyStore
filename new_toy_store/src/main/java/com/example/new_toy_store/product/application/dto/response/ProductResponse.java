package com.example.new_toy_store.product.application.dto.response;

import com.example.new_toy_store.product.domain.ProductStatus;

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
    private ProductStatus statusDetail;
    private List<ProductStatus> allowedNextStatuses;
    private List<String> allowedActions;

    public ProductResponse(Integer id, String name, double basePrice, String status, Integer supplierId,
                           List<Integer> categoryIds, String thumbnailUrl, List<ProductImageResponse> images,
                           double averageRating, int reviewCount,
                           List<ProductVariantResponse> variants, ProductStatus statusDetail,
                           List<ProductStatus> allowedNextStatuses, List<String> allowedActions) {
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
    public ProductStatus getStatusDetail() { return statusDetail; }
    public List<ProductStatus> getAllowedNextStatuses() { return allowedNextStatuses; }
    public List<String> getAllowedActions() { return allowedActions; }
}
