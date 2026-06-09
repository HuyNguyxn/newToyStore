package com.example.new_toy_store.user.application.dto.response;

public class AddressResponse {

    private Integer id;
    private String receiverName;
    private String receiverPhone;
    private String detailAddress;
    private boolean isDefault;

    public AddressResponse(Integer id, String receiverName, String receiverPhone, String detailAddress, boolean isDefault) {
        this.id = id;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
    }

    public Integer getId() { return id; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getDetailAddress() { return detailAddress; }
    public boolean isDefault() { return isDefault; }
}