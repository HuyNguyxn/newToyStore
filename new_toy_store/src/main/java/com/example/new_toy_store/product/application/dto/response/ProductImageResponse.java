package com.example.new_toy_store.product.application.dto.response;

public class ProductImageResponse {

    private Integer id;
    private String imageUrl;
    private boolean thumbnail;

    public ProductImageResponse(Integer id, String imageUrl, boolean thumbnail) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.thumbnail = thumbnail;
    }

    public Integer getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }
}
