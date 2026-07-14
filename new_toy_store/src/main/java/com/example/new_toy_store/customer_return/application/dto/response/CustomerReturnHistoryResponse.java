package com.example.new_toy_store.customer_return.application.dto.response;

import java.time.LocalDateTime;

public class CustomerReturnHistoryResponse {
    private Integer id;
    private String oldStatus;
    private String newStatus;
    private String actionBy;
    private LocalDateTime actionDate;
    private String note;

    public CustomerReturnHistoryResponse() {}

    public CustomerReturnHistoryResponse(Integer id, String oldStatus, String newStatus, String actionBy, LocalDateTime actionDate, String note) {
        this.id = id; this.oldStatus = oldStatus; this.newStatus = newStatus; this.actionBy = actionBy; this.actionDate = actionDate; this.note = note;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getActionBy() { return actionBy; }
    public void setActionBy(String actionBy) { this.actionBy = actionBy; }
    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}