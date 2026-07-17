package com.example.new_toy_store.product.application.dto.response;

import java.time.LocalDate;

public class InventoryBatchResponse {

    private String batchNumber;
    private LocalDate expiryDate;
    private int quantity;

    public InventoryBatchResponse() {
    }

    public InventoryBatchResponse(String batchNumber, LocalDate expiryDate, int quantity) {
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}