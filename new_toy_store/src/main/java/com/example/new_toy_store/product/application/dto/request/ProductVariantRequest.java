package com.example.new_toy_store.product.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public class ProductVariantRequest {

    @NotEmpty(message = "Danh sách thuộc tính không được để trống")
    private Map<String, String> attributes;

    @Min(value = 0, message = "Số lượng tồn kho ban đầu phải lớn hơn hoặc bằng 0")
    private int initialStock;

    @Min(value = 0, message = "Giá bán phải lớn hơn hoặc bằng 0")
    private double price;

    private boolean isMaster = false;

    public Map<String, String> getAttributes() { return attributes; }
    public int getInitialStock() { return initialStock; }
    public double getPrice() { return price; }
    public boolean isMaster() { return isMaster; }
}