package com.example.new_toy_store.supplier.application.dto.request;

import jakarta.validation.constraints.Size;

public class SupplierFilterRequest {
    @Size(max = 150, message = "Từ khóa tìm kiếm tên không được vượt quá 150 ký tự")
    private String name;

    @Size(max = 20, message = "Từ khóa tìm kiếm số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;

    @Size(max = 50, message = "Từ khóa trạng thái không được vượt quá 50 ký tự")
    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}