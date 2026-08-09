package com.example.new_toy_store.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    @Size(max = 72, message = "Mật khẩu cũ không được vượt quá 72 ký tự")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 72, message = "Mật khẩu mới phải có từ 6 đến 72 ký tự")
    private String newPassword;

    public String getOldPassword() { return oldPassword; }
    public String getNewPassword() { return newPassword; }
}
