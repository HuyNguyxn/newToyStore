package com.example.new_toy_store.cart.application.dto.request;

import jakarta.validation.constraints.Min;

public class UpdateCartItemQuantityRequest {

    @Min(value = 1, message = "Số lượng cập nhật phải lớn hơn 0")
    private int quantity;

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
