package com.example.new_toy_store.user.application.dto.request;

import com.example.new_toy_store.user.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

    @NotNull(message = "Trạng thái người dùng không được để trống")
    private UserStatus status;

    public UserStatus getStatus() { return status; }
}
