package com.example.new_toy_store.product.application.dto.response;

import com.example.new_toy_store.product.domain.VariantType;

import java.util.List;
import java.util.Map;

public class ProductVariantResponse {

    private Integer id;
    private String type;
    private double price;
    private double discountedPrice;
    private int stockQuantity;
    private Map<String, String> attributes;
    private VariantType typeDetail;
    private List<VariantType> allowedNextTypes;
    private List<String> allowedActions;

    public ProductVariantResponse(Integer id, String type, double price, double discountedPrice,
                                  int stockQuantity, Map<String, String> attributes,
                                  VariantType typeDetail, List<VariantType> allowedNextTypes,
                                  List<String> allowedActions) {
        this.id = id;
        this.type = type;
        this.price = price;
        this.discountedPrice = discountedPrice;
        this.stockQuantity = stockQuantity;
        this.attributes = attributes;
        this.typeDetail = typeDetail;
        this.allowedNextTypes = allowedNextTypes == null ? List.of() : List.copyOf(allowedNextTypes);
        this.allowedActions = allowedActions == null ? List.of() : List.copyOf(allowedActions);
    }

    public Integer getId() { return id; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public double getDiscountedPrice() { return discountedPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public Map<String, String> getAttributes() { return attributes; }
    public VariantType getTypeDetail() { return typeDetail; }
    public List<VariantType> getAllowedNextTypes() { return allowedNextTypes; }
    public List<String> getAllowedActions() { return allowedActions; }
}
