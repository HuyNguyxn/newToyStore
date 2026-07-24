package com.example.new_toy_store.review.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class ReviewFilterRequest {

    private Integer productId;

    @Min(value = 1, message = "Điểm đánh giá bộ lọc thấp nhất là 1 sao")
    @Max(value = 5, message = "Điểm đánh giá bộ lọc cao nhất là 5 sao")
    private Integer rating;

    private Boolean hasAdminReplied;

    @Pattern(regexp = "^(PUBLISHED|HIDDEN)$", message = "Trạng thái lọc chỉ chấp nhận PUBLISHED hoặc HIDDEN")
    private String status;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Boolean getHasAdminReplied() {
        return hasAdminReplied;
    }

    public void setHasAdminReplied(Boolean hasAdminReplied) {
        this.hasAdminReplied = hasAdminReplied;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
