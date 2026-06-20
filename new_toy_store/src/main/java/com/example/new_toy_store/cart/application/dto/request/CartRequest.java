package com.example.new_toy_store.cart.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CartRequest {

    @NotEmpty(message = "Danh sách sản phẩm đồng bộ không được để trống")
    @Valid
    private List<CartItemRequest> items;
    public List<CartItemRequest> getItems() { return items; }
    public void setItems(List<CartItemRequest> items) { this.items = items; }
}