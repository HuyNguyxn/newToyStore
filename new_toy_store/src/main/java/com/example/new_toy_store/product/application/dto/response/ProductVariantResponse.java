package com.example.new_toy_store.product.application.dto.response;

import java.util.Map;

public class ProductVariantResponse {

    private Integer id;
    private String type;
    private double price;
    private double discountedPrice;
    private int stockQuantity;
    private Map<String, String> attributes;

    public ProductVariantResponse(Integer id, String type, double price, double discountedPrice, int stockQuantity, Map<String, String> attributes) {
        this.id = id;
        this.type = type;
        this.price = price;
        this.discountedPrice = discountedPrice;
        this.stockQuantity = stockQuantity;
        this.attributes = attributes;
    }

    public Integer getId() { return id; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public double getDiscountedPrice() { return discountedPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public Map<String, String> getAttributes() { return attributes; }
}