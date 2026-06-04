package com.example.new_toy_store.cart.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartItemRequest {

    @NotNull(message = "ProductId is required")
    private Integer productId;

    @NotNull(message = "VariantId is required")
    private Integer variantId;

    @Min(value = 1, message = "Quantity must be > 0")
    private int quantity;

    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public int getQuantity() { return quantity; }
}