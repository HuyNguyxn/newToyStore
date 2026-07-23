package com.example.new_toy_store.product.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddProductImageRequest {

    @NotBlank(message = "Đường dẫn hình ảnh không được để trống")
    @Size(max = 1000, message = "Đường dẫn hình ảnh không được vượt quá 1000 ký tự")
    private String imageUrl;

    private boolean thumbnail;

    public String getImageUrl() { return imageUrl; }
    public boolean isThumbnail() { return thumbnail; }
}
