package com.example.new_toy_store.supplier_return.application.dto.response;

import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;

import java.time.LocalDateTime;

public class SupplierReturnHistoryResponse {
    private Integer id;
    private SupplierReturnStatus oldStatus;
    private SupplierReturnStatus newStatus;
    private String actionBy;
    private String note;
    private LocalDateTime createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public SupplierReturnStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(SupplierReturnStatus oldStatus) { this.oldStatus = oldStatus; }
    public SupplierReturnStatus getNewStatus() { return newStatus; }
    public void setNewStatus(SupplierReturnStatus newStatus) { this.newStatus = newStatus; }
    public String getActionBy() { return actionBy; }
    public void setActionBy(String actionBy) { this.actionBy = actionBy; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
