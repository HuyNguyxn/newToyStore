package com.example.new_toy_store.product.application.dto.request;

import jakarta.validation.constraints.Min;

public class AddVariantStockRequest {

    @Min(value = 1, message = "Số lượng nhập kho phải lớn hơn 0")
    private int amount;

    public int getAmount() { return amount; }
}
