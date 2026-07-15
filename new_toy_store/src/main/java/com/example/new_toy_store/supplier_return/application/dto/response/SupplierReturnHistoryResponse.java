package com.example.new_toy_store.supplier_return.application.dto.response;

import java.time.LocalDateTime;

public class SupplierReturnHistoryResponse {
    private Integer id;
    private String oldStatus;
    private String newStatus;
    private String actionBy;
    private String note;
    private LocalDateTime createdAt;

    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public String getOldStatus() { return oldStatus; } public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; } public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getActionBy() { return actionBy; } public void setActionBy(String actionBy) { this.actionBy = actionBy; }
    public String getNote() { return note; } public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}