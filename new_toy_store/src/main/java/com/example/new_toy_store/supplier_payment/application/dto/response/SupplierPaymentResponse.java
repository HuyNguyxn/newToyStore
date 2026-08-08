package com.example.new_toy_store.supplier_payment.application.dto.response;

import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SupplierPaymentResponse {
    private final Integer id;
    private final Integer supplierId;
    private final String supplierName;
    private final Integer importNoteId;
    private final String invoiceCode;
    private final SupplierPaymentStatus status;
    private final List<SupplierPaymentActionResponse> allowedNextActions;
    private final double totalAmount;
    private final double paidAmount;
    private final double remainingAmount;
    private final LocalDate dueDate;
    private final String note;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<SupplierPaymentTransactionResponse> transactions;

    public SupplierPaymentResponse(
            Integer id,
            Integer supplierId,
            String supplierName,
            Integer importNoteId,
            String invoiceCode,
            SupplierPaymentStatus status,
            List<SupplierPaymentActionResponse> allowedNextActions,
            double totalAmount,
            double paidAmount,
            double remainingAmount,
            LocalDate dueDate,
            String note,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<SupplierPaymentTransactionResponse> transactions
    ) {
        this.id = id;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.importNoteId = importNoteId;
        this.invoiceCode = invoiceCode;
        this.status = status;
        this.allowedNextActions = allowedNextActions;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.remainingAmount = remainingAmount;
        this.dueDate = dueDate;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.transactions = transactions;
    }

    public Integer getId() { return id; }
    public Integer getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public Integer getImportNoteId() { return importNoteId; }
    public String getInvoiceCode() { return invoiceCode; }
    public SupplierPaymentStatus getStatus() { return status; }
    public List<SupplierPaymentActionResponse> getAllowedNextActions() { return allowedNextActions; }
    public double getTotalAmount() { return totalAmount; }
    public double getPaidAmount() { return paidAmount; }
    public double getRemainingAmount() { return remainingAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<SupplierPaymentTransactionResponse> getTransactions() { return transactions; }
}
