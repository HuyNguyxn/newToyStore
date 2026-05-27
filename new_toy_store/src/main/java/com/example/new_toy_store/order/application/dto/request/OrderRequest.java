package com.example.new_toy_store.order.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class OrderRequest {

    @NotEmpty(message = "Items cannot be empty")
    @Valid
    private List<OrderItemRequest> items;

    public List<OrderItemRequest> getItems() {
        return items;
    }
}