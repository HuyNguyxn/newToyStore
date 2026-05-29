package com.example.new_toy_store.product.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ProductRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Min(value = 0, message = "Base price must be >= 0")
    private double basePrice;

    @NotNull(message = "CategoryId is required")
    private Integer categoryId;

    @Min(value = 0, message = "Initial stock must be >= 0")
    private int defaultInitialStock;

    @Valid
    private List<ProductVariantRequest> variants;

    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public Integer getCategoryId() { return categoryId; }
    public int getDefaultInitialStock() { return defaultInitialStock; }
    public List<ProductVariantRequest> getVariants() { return variants; }
}