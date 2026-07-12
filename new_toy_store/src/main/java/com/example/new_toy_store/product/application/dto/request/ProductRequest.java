package com.example.new_toy_store.product.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @Min(value = 0, message = "Giá bán không được nhỏ hơn 0")
    private double basePrice;

    @NotEmpty(message = "Cần chọn ít nhất một danh mục")
    private List<Integer> categoryIds;

    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    private String status;

    @Min(value = 0, message = "Số lượng tồn kho ban đầu không được nhỏ hơn 0")
    private int defaultInitialStock;

    @NotNull
    private Integer supplierId;

    @Valid
    private List<ProductVariantRequest> variants;

    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public List<Integer> getCategoryIds() { return categoryIds; }
    public String getStatus() { return status; }
    public int getDefaultInitialStock() { return defaultInitialStock; }
    public Integer getSupplierId() { return supplierId; }
    public List<ProductVariantRequest> getVariants() { return variants; }
}