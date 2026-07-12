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
    private double averageRating;
    private int reviewCount;
    private List<ProductVariantResponse> variants;

    public ProductResponse(Integer id, String name, double basePrice, String status, Integer supplierId,
                           List<Integer> categoryIds, double averageRating, int reviewCount,
                           List<ProductVariantResponse> variants) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.status = status;
        this.supplierId = supplierId;
        this.categoryIds = categoryIds;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.variants = variants;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public String getStatus() { return status; }
    public Integer getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public List<Integer> getCategoryIds() { return categoryIds; }
    public double getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
    public List<ProductVariantResponse> getVariants() { return variants; }
}