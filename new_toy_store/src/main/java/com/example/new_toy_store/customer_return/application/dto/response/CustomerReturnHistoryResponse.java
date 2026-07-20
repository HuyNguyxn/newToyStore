package com.example.new_toy_store.customer_return.application.dto.response;

import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import java.time.LocalDateTime;

public class CustomerReturnHistoryResponse {
    private Integer id;
    private CustomerReturnStatus oldStatus;
    private CustomerReturnStatus newStatus;
    private String actionBy;
    private LocalDateTime actionDate;
    private String note;

    public CustomerReturnHistoryResponse() {}

    public CustomerReturnHistoryResponse(Integer id, CustomerReturnStatus oldStatus, CustomerReturnStatus newStatus, String actionBy, LocalDateTime actionDate, String note) {
        this.id = id;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.actionBy = actionBy;
        this.actionDate = actionDate;
        this.note = note;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public CustomerReturnStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(CustomerReturnStatus oldStatus) { this.oldStatus = oldStatus; }
    public CustomerReturnStatus getNewStatus() { return newStatus; }
    public void setNewStatus(CustomerReturnStatus newStatus) { this.newStatus = newStatus; }
    public String getActionBy() { return actionBy; }
    public void setActionBy(String actionBy) { this.actionBy = actionBy; }
    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}