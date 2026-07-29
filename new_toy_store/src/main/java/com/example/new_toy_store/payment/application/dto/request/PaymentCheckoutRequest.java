package com.example.new_toy_store.payment.application.dto.request;

import com.example.new_toy_store.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PaymentCheckoutRequest {

    @NotNull(message = "Order id must not be empty")
    private Integer orderId;

    @NotNull(message = "Payment method must not be empty")
    private PaymentMethod method;

    @Size(max = 80, message = "Idempotency key must not exceed 80 characters")
    private String idempotencyKey;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
