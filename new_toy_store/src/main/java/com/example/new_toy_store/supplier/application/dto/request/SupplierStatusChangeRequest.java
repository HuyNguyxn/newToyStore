package com.example.new_toy_store.supplier.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupplierStatusChangeRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    private String status;

    public String getStatus() { return status; }
}
