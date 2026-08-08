package com.example.new_toy_store.supplier_payment.application.dto.response;

import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SupplierPaymentTransactionResponse {
    private final Integer id;
    private final double amount;
    private final SupplierPaymentMethod method;
    private final String referenceCode;
    private final LocalDate paidDate;
    private final String note;
    private final LocalDateTime createdAt;

    public SupplierPaymentTransactionResponse(
            Integer id,
            double amount,
            SupplierPaymentMethod method,
            String referenceCode,
            LocalDate paidDate,
            String note,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.amount = amount;
        this.method = method;
        this.referenceCode = referenceCode;
        this.paidDate = paidDate;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public double getAmount() { return amount; }
    public SupplierPaymentMethod getMethod() { return method; }
    public String getReferenceCode() { return referenceCode; }
    public LocalDate getPaidDate() { return paidDate; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
