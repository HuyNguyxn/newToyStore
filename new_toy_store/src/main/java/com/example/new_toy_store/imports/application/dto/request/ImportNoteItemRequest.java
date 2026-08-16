package com.example.new_toy_store.imports.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class ImportNoteItemRequest {
    @NotNull(message = "Mã sản phẩm không được để trống")
    private Integer productId;

    @NotNull(message = "Mã biến thể không được để trống")
    private Integer variantId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0")
    private int quantity;

    @Min(value = 0, message = "Giá nhập không được âm")
    private double importPrice;

    @PositiveOrZero(message = "Giá bán mới không được âm")
    private Double sellingPrice;

    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getImportPrice() { return importPrice; }
    public Double getSellingPrice() { return sellingPrice; }
}
