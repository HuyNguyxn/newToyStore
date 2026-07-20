package com.example.new_toy_store.customer_return.application.dto.response;

import com.example.new_toy_store.customer_return.domain.ReturnReasonCode;

public class CustomerReturnItemResponse {
    private Integer id;
    private Integer orderItemId;
    private Integer productId;
    private Integer variantId;
    private Integer quantity;
    private ReturnReasonCode reasonCode;
    private double expectedRefundAmount;

    public CustomerReturnItemResponse() {}

    public CustomerReturnItemResponse(Integer id, Integer orderItemId, Integer quantity, ReturnReasonCode reasonCode, double expectedRefundAmount) {
        this.id = id;
        this.orderItemId = orderItemId;
        this.quantity = quantity;
        this.reasonCode = reasonCode;
        this.expectedRefundAmount = expectedRefundAmount;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public ReturnReasonCode getReasonCode() { return reasonCode; }
    public void setReasonCode(ReturnReasonCode reasonCode) { this.reasonCode = reasonCode; }
    public double getExpectedRefundAmount() { return expectedRefundAmount; }
    public void setExpectedRefundAmount(double expectedRefundAmount) { this.expectedRefundAmount = expectedRefundAmount; }
}