package com.example.new_toy_store.order.application.dto.request;

public class OrderItemRequest {

    private String productName;
    private int quantity;
    private double price;

    public String getProductName() { return productName; }

    public int getQuantity() { return quantity; }

    public double getPrice() { return price; }
}