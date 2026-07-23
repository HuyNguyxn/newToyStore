package com.example.new_toy_store.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRoleRequest {

    @NotBlank(message = "Vai trò người dùng không được để trống")
    private String role;

    public String getRole() { return role; }
}
