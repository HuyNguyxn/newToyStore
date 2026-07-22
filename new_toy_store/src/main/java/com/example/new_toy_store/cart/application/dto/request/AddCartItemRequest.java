package com.example.new_toy_store.cart.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AddCartItemRequest {

    @NotNull(message = "ID sản phẩm không được để trống")
    @Positive(message = "ID sản phẩm phải lớn hơn 0")
    private Integer productId;

    @NotNull(message = "ID biến thể không được để trống")
    @Positive(message = "ID biến thể phải lớn hơn 0")
    private Integer variantId;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
