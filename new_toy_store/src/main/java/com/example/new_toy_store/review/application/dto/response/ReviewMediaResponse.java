package com.example.new_toy_store.review.application.dto.response;

import com.example.new_toy_store.review.domain.ReviewMediaType;

public class ReviewMediaResponse {

    private Integer id;
    private ReviewMediaType mediaType;
    private String mediaUrl;
    private int displayOrder;

    public ReviewMediaResponse(Integer id, ReviewMediaType mediaType, String mediaUrl, int displayOrder) {
        this.id = id;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.displayOrder = displayOrder;
    }

    public Integer getId() {
        return id;
    }

    public ReviewMediaType getMediaType() {
        return mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
