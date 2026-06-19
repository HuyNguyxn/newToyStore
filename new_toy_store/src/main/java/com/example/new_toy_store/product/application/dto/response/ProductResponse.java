package com.example.new_toy_store.product.application.dto.response;

import java.util.List;

public class ProductResponse {

    private Integer id;
    private String name;
    private double basePrice;
    private String status;
    private Integer supplierId;
    private List<Integer> categoryIds;
    private List<ProductVariantResponse> variants;

    public ProductResponse(Integer id, String name, double basePrice, String status, Integer supplierId, List<Integer> categoryIds, List<ProductVariantResponse> variants) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.status = status;
        this.supplierId = supplierId;
        this.categoryIds = categoryIds;
        this.variants = variants;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public String getStatus() { return status; }
    public Integer getSupplierId() { return supplierId; }
    public List<Integer> getCategoryIds() { return categoryIds; }
    public List<ProductVariantResponse> getVariants() { return variants; }
}