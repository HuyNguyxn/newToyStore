package com.example.new_toy_store.cart.application.dto.request;

import com.example.new_toy_store.cart.domain.Cart;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SyncCartRequest {

    @NotEmpty(message = "Danh sách sản phẩm đồng bộ không được để trống")
    @Size(max = Cart.MAX_CART_ITEMS, message = "Danh sách sản phẩm không được vượt quá 50 mục")
    @Valid
    private List<SyncCartItemRequest> items;

    public List<SyncCartItemRequest> getItems() { return items; }
    public void setItems(List<SyncCartItemRequest> items) { this.items = items; }
}
