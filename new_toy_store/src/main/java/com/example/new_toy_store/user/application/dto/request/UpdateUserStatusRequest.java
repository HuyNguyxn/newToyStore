package com.example.new_toy_store.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserStatusRequest {

    @NotBlank(message = "Trạng thái người dùng không được để trống")
    private String status;

    public String getStatus() { return status; }
}
