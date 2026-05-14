package com.example.new_toy_store.order.dto.request;

import java.util.List;

public class OrderRequest {

    private String status;
    private List<OrderItemRequest> items;

    public String getStatus() { return status; }
    public List<OrderItemRequest> getItems() { return items; }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}