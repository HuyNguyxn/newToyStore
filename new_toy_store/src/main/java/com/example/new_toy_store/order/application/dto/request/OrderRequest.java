package com.example.new_toy_store.order.application.dto.request;

import lombok.NonNull;

import java.util.List;

public class OrderRequest {

    private String status;
    private List<OrderItemRequest> items;

    public String getStatus() { return status; }
    public List<OrderItemRequest> getItems() { return items; }

    @NonNull
    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}