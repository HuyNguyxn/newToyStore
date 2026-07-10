package com.example.new_toy_store.review.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewCreateRequest {
    @NotNull(message = "Mã chi tiết đơn hàng (OrderItemId) không được để trống")
    private Integer orderItemId;

    @Min(value = 1, message = "Điểm đánh giá thấp nhất là 1 sao")
    @Max(value = 5, message = "Điểm đánh giá cao nhất là 5 sao")
    private int rating;

    @Size(max = 1000, message = "Nội dung bình luận không được vượt quá 1000 ký tự")
    private String comment;

    public Integer getOrderItemId() { return orderItemId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}