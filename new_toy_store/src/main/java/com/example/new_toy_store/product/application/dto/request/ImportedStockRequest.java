package com.example.new_toy_store.product.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ImportedStockRequest {

    @NotNull(message = "Mã biến thể (variantId) không được để trống")
    private Integer variantId;

    @Min(value = 1, message = "Số lượng nhập kho phải lớn hơn hoặc bằng 1")
    private int quantity;

    @Min(value = 0, message = "Giá nhập kho không được là số âm")
    private double importPrice;

    public ImportedStockRequest(Integer variantId, int quantity, double importPrice) {
        this.variantId = variantId;
        this.quantity = quantity;
        this.importPrice = importPrice;
    }

    public Integer getVariantId() { return variantId; }
    public int getQuantity() { return quantity; }
    public double getImportPrice() { return importPrice; }
}