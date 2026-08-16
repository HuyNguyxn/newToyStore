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

    private String batchNumber;
    private Integer productId;
    private Double sellingPrice;

    public ImportedStockRequest(Integer productId, Integer variantId, int quantity, double importPrice, Double sellingPrice, String batchNumber) {
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.importPrice = importPrice;
        this.sellingPrice = sellingPrice;
        this.batchNumber = batchNumber;
    }

    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public int getQuantity() { return quantity; }
    public double getImportPrice() { return importPrice; }
    public String getBatchNumber() { return batchNumber; }
    public Double getSellingPrice() { return sellingPrice; }
}
