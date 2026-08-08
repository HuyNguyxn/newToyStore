package com.example.new_toy_store.customer_payment.application.dto.request;

import com.example.new_toy_store.customer_payment.domain.CustomerPaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CustomerPaymentCheckoutRequest {

    @NotNull(message = "Order id must not be empty")
    private Integer orderId;

    @NotNull(message = "Payment method must not be empty")
    private CustomerPaymentMethod method;

    @Size(max = 80, message = "Idempotency key must not exceed 80 characters")
    private String idempotencyKey;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public CustomerPaymentMethod getMethod() { return method; }
    public void setMethod(CustomerPaymentMethod method) { this.method = method; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
