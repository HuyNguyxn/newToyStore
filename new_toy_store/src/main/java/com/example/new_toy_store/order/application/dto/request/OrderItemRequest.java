package com.example.new_toy_store.order.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequest {

    @NotNull(message = "ProductId is required")
    private Integer productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @Min(value = 1, message = "Quantity must be > 0")
    private int quantity;

    @Min(value = 1, message = "Price must be > 0")
    private double price;

    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}