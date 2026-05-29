package com.example.new_toy_store.product.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public class ProductVariantRequest {

    @NotEmpty(message = "Attributes map is required")
    private Map<String, String> attributes;

    @Min(value = 0, message = "Initial stock must be >= 0")
    private int initialStock;

    @Min(value = 0, message = "Price must be >= 0")
    private double price;

    private boolean isMaster = false;

    public Map<String, String> getAttributes() { return attributes; }
    public int getInitialStock() { return initialStock; }
    public double getPrice() { return price; }
    public boolean isMaster() { return isMaster; }
}