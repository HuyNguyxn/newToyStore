package com.example.new_toy_store.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AddressRequest {

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    private String receiverPhone;

    @NotBlank(message = "Detail address is required")
    private String detailAddress;

    private boolean isDefault;

    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getDetailAddress() { return detailAddress; }
    public boolean isDefault() { return isDefault; }
}