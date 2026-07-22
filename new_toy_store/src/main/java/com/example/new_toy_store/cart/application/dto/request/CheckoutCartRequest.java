package com.example.new_toy_store.cart.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CheckoutCartRequest {

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 500, message = "Địa chỉ giao hàng không được vượt quá 500 ký tự")
    private String shippingAddress;

    @Size(max = 50, message = "Mã giảm giá không được vượt quá 50 ký tự")
    private String promoCode;

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
}
