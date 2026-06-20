package com.example.new_toy_store.cart.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartItemRequest {

    @NotNull(message = "ID sản phẩm không được để trống")
    private Integer productId;

    @NotNull(message = "ID biến thể không được để trống")
    private Integer variantId;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;

    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public int getQuantity() { return quantity; }
}