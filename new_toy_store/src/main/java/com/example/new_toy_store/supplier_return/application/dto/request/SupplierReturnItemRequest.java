package com.example.new_toy_store.supplier_return.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class SupplierReturnItemRequest {

    @NotNull(message = "Mã sản phẩm không được trống")
    private Integer productId;

    @NotNull(message = "Mã biến thể không được trống")
    private Integer variantId;

    @NotBlank(message = "Tên sản phẩm không được trống")
    private String productName;

    @Min(value = 1, message = "Số lượng xuất trả phải lớn hơn hoặc bằng 1")
    private int quantity;

    @Min(value = 0, message = "Đơn giá trả không được âm")
    private double returnPrice;

    @Min(value = 0, message = "Chiết khấu không được âm")
    private double discountAmount;

    @NotBlank(message = "Mã lý do trả hàng không được trống")
    private String reasonCode;

    @NotBlank(message = "Bắt buộc phải truyền mã lô")
    private String batchNumber;

    @NotNull(message = "Bắt buộc phải có hạn sử dụng của lô hàng")
    private LocalDate expiryDate;

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getReturnPrice() { return returnPrice; }
    public void setReturnPrice(double returnPrice) { this.returnPrice = returnPrice; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}
