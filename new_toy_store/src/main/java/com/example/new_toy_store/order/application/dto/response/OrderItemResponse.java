package com.example.new_toy_store.order.application.dto.response;

public class OrderItemResponse {

    private String productName;
    private int quantity;
    private double price;

    public OrderItemResponse(String productName, int quantity, double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}