package com.example.new_toy_store.customer_return.application.dto.response;

public class CustomerReturnItemResponse {
    private Integer id;
    private Integer orderItemId;
    private Integer quantity;
    private String reasonCode;
    private String reasonDescription;
    private double expectedRefundAmount;

    public CustomerReturnItemResponse() {}

    public CustomerReturnItemResponse(Integer id, Integer orderItemId, Integer quantity, String reasonCode, String reasonDescription, double expectedRefundAmount) {
        this.id = id; this.orderItemId = orderItemId; this.quantity = quantity; this.reasonCode = reasonCode; this.reasonDescription = reasonDescription; this.expectedRefundAmount = expectedRefundAmount;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getReasonDescription() { return reasonDescription; }
    public void setReasonDescription(String reasonDescription) { this.reasonDescription = reasonDescription; }
    public double getExpectedRefundAmount() { return expectedRefundAmount; }
    public void setExpectedRefundAmount(double expectedRefundAmount) { this.expectedRefundAmount = expectedRefundAmount; }
}