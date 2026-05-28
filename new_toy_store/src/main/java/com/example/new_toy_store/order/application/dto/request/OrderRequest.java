package com.example.new_toy_store.order.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderRequest {

    @NotNull(message = "UserId is required")
    private Integer userId;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotEmpty(message = "Items cannot be empty")
    @Valid
    private List<OrderItemRequest> items;

    public Integer getUserId() { return userId; }
    public String getShippingAddress() { return shippingAddress; }
    public List<OrderItemRequest> getItems() { return items; }
}