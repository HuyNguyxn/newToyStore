package com.example.new_toy_store.supplier.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SupplierRequest {

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    private String email;
    private String address;
    private String status;

    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
}