package com.example.new_toy_store.supplier_payment.application.dto.request;

import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class SupplierPaymentRecordRequest {
    @DecimalMin(value = "0.01", message = "Số tiền thanh toán phải lớn hơn 0")
    private double amount;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private SupplierPaymentMethod method;

    @Size(max = 100, message = "Mã tham chiếu không được vượt quá 100 ký tự")
    private String referenceCode;

    private LocalDate paidDate;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note;

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public SupplierPaymentMethod getMethod() { return method; }
    public void setMethod(SupplierPaymentMethod method) { this.method = method; }
    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }
    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
