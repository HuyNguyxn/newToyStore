package com.example.new_toy_store.supplier_payment.application.dto.request;

public class SupplierPaymentFilterRequest {
    private Integer supplierId;
    private Integer importNoteId;
    private String status;

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }
    public Integer getImportNoteId() { return importNoteId; }
    public void setImportNoteId(Integer importNoteId) { this.importNoteId = importNoteId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
