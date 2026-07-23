package com.example.new_toy_store.user.application.dto.request;

import com.example.new_toy_store.user.domain.UserRole;
import jakarta.validation.constraints.NotNull;

public class UpdateUserRoleRequest {

    @NotNull(message = "Vai trò người dùng không được để trống")
    private UserRole role;

    public UserRole getRole() { return role; }
}
