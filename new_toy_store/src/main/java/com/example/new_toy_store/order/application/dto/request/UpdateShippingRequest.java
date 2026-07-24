package com.example.new_toy_store.order.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateShippingRequest {

    @NotBlank(message = "Địa chỉ giao hàng mới không được để trống")
    @Size(max = 255, message = "Địa chỉ giao hàng không được vượt quá 255 ký tự")
    private String newAddress;

    private String note;

    public String getNewAddress() { return newAddress; }
    public void setNewAddress(String newAddress) { this.newAddress = newAddress; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
