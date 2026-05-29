package com.example.new_toy_store.product.application.dto.response;

import java.util.List;

public class ProductResponse {

    private Integer id;
    private String name;
    private double basePrice;
    private Integer categoryId;
    private List<ProductVariantResponse> variants;

    public ProductResponse(Integer id, String name, double basePrice, Integer categoryId, List<ProductVariantResponse> variants) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.categoryId = categoryId;
        this.variants = variants;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public Integer getCategoryId() { return categoryId; }
    public List<ProductVariantResponse> getVariants() { return variants; }
}