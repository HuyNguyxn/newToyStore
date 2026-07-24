package com.example.new_toy_store.customer_return.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class CustomerReturnItemRequest {

    @NotNull(message = "Mã sản phẩm trong đơn hàng không được để trống")
    private Integer orderItemId;

    @NotNull(message = "Mã sản phẩm không được để trống")
    private Integer productId;

    @NotNull(message = "Mã biến thể không được để trống")
    private Integer variantId;

    @NotNull(message = "Số lượng trả lại không được để trống")
    @Min(value = 1, message = "Số lượng trả lại ít nhất phải là 1")
    private Integer quantity;

    @NotBlank(message = "Mã lý do trả hàng không được để trống")
    private String reasonCode;

    @NotNull(message = "Số tiền hoàn dự kiến không được để trống")
    @PositiveOrZero(message = "Số tiền hoàn dự kiến không được là số âm")
    private Double expectedRefundAmount;

    public CustomerReturnItemRequest() {}

    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public Double getExpectedRefundAmount() { return expectedRefundAmount; }
    public void setExpectedRefundAmount(Double expectedRefundAmount) { this.expectedRefundAmount = expectedRefundAmount; }
}
