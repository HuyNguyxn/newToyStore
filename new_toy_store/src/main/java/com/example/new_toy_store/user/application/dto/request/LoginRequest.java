package com.example.new_toy_store.user.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 254, message = "Email không được vượt quá 254 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(max = 72, message = "Mật khẩu không được vượt quá 72 ký tự")
    private String password;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
