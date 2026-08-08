package com.example.new_toy_store.imports.application.dto.response;

import com.example.new_toy_store.imports.domain.ImportStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ImportNoteResponse {
    private Integer id;
    private Integer supplierId;
    private String supplierName;
    private String supplierPhoneNumber;
    private ImportStatus status;
    private List<ImportStatusActionResponse> allowedNextActions;
    private double totalAmount;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private List<ImportNoteItemResponse> items;

    public ImportNoteResponse(Integer id,
                              Integer supplierId,
                              String supplierName,
                              String supplierPhoneNumber,
                              ImportStatus status,
                              List<ImportStatusActionResponse> allowedNextActions,
                              double totalAmount,
                              String note,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt,
                              String createdBy,
                              List<ImportNoteItemResponse> items) {
        this.id = id;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierPhoneNumber = supplierPhoneNumber;
        this.status = status;
        this.allowedNextActions = allowedNextActions;
        this.totalAmount = totalAmount;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.items = items;
    }

    public Integer getId() { return id; }
    public Integer getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getSupplierPhoneNumber() { return supplierPhoneNumber; }
    public ImportStatus getStatus() { return status; }
    public List<ImportStatusActionResponse> getAllowedNextActions() { return allowedNextActions; }
    public double getTotalAmount() { return totalAmount; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public List<ImportNoteItemResponse> getItems() { return items; }
}
