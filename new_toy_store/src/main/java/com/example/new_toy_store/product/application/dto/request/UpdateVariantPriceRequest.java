package com.example.new_toy_store.product.application.dto.request;

import jakarta.validation.constraints.DecimalMin;

public class UpdateVariantPriceRequest {

    @DecimalMin(value = "0.0", message = "Giá bán không được nhỏ hơn 0")
    private double price;

    public double getPrice() { return price; }
}
