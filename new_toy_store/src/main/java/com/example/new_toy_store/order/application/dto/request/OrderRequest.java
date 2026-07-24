package com.example.new_toy_store.order.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class OrderRequest {

    private Integer userId;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 255, message = "Địa chỉ giao hàng không được vượt quá 255 ký tự")
    private String shippingAddress;

    @Size(max = 50, message = "Mã khuyến mãi không được vượt quá 50 ký tự")
    private String promoCode;

    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    @Valid
    private List<OrderItemRequest> items;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public List<OrderItemRequest> getItems() { return items; }
}
