package com.example.new_toy_store.imports.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ImportNoteItemRequest {
    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Variant ID is required")
    private Integer variantId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @Min(value = 1, message = "Quantity must be > 0")
    private int quantity;

    @Min(value = 0, message = "Import price must be >= 0")
    private double importPrice;

    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getImportPrice() { return importPrice; }
}