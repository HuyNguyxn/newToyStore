package com.example.new_toy_store.product.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateProductStatusRequest {

    @NotBlank(message = "Trạng thái sản phẩm không được để trống")
    private String status;

    public UpdateProductStatusRequest() {
    }

    public UpdateProductStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
