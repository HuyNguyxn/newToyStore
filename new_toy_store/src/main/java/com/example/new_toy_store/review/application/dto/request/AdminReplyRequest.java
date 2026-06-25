package com.example.new_toy_store.review.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminReplyRequest {
    @NotBlank(message = "Nội dung phản hồi không được để trống")
    @Size(max = 1000, message = "Nội dung phản hồi không được vượt quá 1000 ký tự")
    private String reply;

    public String getReply() { return reply; }
}