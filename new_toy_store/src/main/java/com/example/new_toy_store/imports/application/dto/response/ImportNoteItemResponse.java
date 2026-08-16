package com.example.new_toy_store.imports.application.dto.response;

public class ImportNoteItemResponse {
    private Integer id;
    private Integer productId;
    private Integer variantId;
    private String productName;
    private int quantity;
    private double importPrice;
    private Double sellingPrice;
    private double totalPrice;

    public ImportNoteItemResponse(Integer id, Integer productId, Integer variantId, String productName, int quantity, double importPrice, Double sellingPrice, double totalPrice) {
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.quantity = quantity;
        this.importPrice = importPrice;
        this.sellingPrice = sellingPrice;
        this.totalPrice = totalPrice;
    }

    public Integer getId() { return id; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getImportPrice() { return importPrice; }
    public Double getSellingPrice() { return sellingPrice; }
    public double getTotalPrice() { return totalPrice; }
}
