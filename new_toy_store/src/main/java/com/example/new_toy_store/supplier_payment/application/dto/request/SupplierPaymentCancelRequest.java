package com.example.new_toy_store.supplier_payment.application.dto.request;

import jakarta.validation.constraints.Size;

public class SupplierPaymentCancelRequest {
    @Size(max = 500, message = "Lý do hủy không được vượt quá 500 ký tự")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
