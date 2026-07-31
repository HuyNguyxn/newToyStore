package com.example.new_toy_store.review.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ReviewCreateRequest {
    @NotNull(message = "Mã chi tiết đơn hàng (orderItemId) không được để trống")
    private Integer orderItemId;

    @Min(value = 1, message = "Điểm đánh giá thấp nhất là 1 sao")
    @Max(value = 5, message = "Điểm đánh giá cao nhất là 5 sao")
    private int rating;

    @Size(max = 1000, message = "Nội dung bình luận không được vượt quá 1000 ký tự")
    private String comment;

    @Size(max = 5, message = "Một đánh giá chỉ được đính kèm tối đa 5 hình ảnh")
    private List<
            @Size(max = 1000, message = "URL hình ảnh không được vượt quá 1000 ký tự")
            @Pattern(regexp = "^https?://.+", message = "URL hình ảnh phải bắt đầu bằng http:// hoặc https://")
            String> imageUrls;

    @Size(max = 2, message = "Một đánh giá chỉ được đính kèm tối đa 2 video")
    private List<
            @Size(max = 1000, message = "URL video không được vượt quá 1000 ký tự")
            @Pattern(regexp = "^https?://.+", message = "URL video phải bắt đầu bằng http:// hoặc https://")
            String> videoUrls;

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public List<String> getVideoUrls() {
        return videoUrls;
    }
}
