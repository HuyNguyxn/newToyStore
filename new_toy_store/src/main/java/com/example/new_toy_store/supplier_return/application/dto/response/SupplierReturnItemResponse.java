package com.example.new_toy_store.supplier_return.application.dto.response;

import com.example.new_toy_store.supplier_return.domain.SupplierReturnReason;

import java.time.LocalDate;

public class SupplierReturnItemResponse {

    private Integer id;
    private Integer productId;
    private Integer variantId;
    private String productName;
    private int quantity;
    private int acceptedQuantity;
    private double returnPrice;
    private double discountAmount;
    private SupplierReturnReason reason;
    private String discrepancyReason;
    private String batchNumber;
    private LocalDate expiryDate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getAcceptedQuantity() { return acceptedQuantity; }
    public void setAcceptedQuantity(int acceptedQuantity) { this.acceptedQuantity = acceptedQuantity; }
    public double getReturnPrice() { return returnPrice; }
    public void setReturnPrice(double returnPrice) { this.returnPrice = returnPrice; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public SupplierReturnReason getReason() { return reason; }
    public void setReason(SupplierReturnReason reason) { this.reason = reason; }
    public String getDiscrepancyReason() { return discrepancyReason; }
    public void setDiscrepancyReason(String discrepancyReason) { this.discrepancyReason = discrepancyReason; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}
