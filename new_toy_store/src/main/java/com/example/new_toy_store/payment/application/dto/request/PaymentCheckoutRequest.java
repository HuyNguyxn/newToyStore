package com.example.new_toy_store.payment.application.dto.request;

import com.example.new_toy_store.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class PaymentCheckoutRequest {

    @NotNull(message = "Order id must not be empty")
    private Integer orderId;

    @NotNull(message = "Payment method must not be empty")
    private PaymentMethod method;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
}
