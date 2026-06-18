package com.example.new_toy_store.imports.application.dto.response;

import java.util.List;

public class ImportNoteResponse {
    private Integer id;
    private Integer supplierId;
    private String status;
    private double totalAmount;
    private String note;
    private List<ImportNoteItemResponse> items;

    public ImportNoteResponse(Integer id, Integer supplierId, String status, double totalAmount, String note, List<ImportNoteItemResponse> items) {
        this.id = id;
        this.supplierId = supplierId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.note = note;
        this.items = items;
    }

    public Integer getId() { return id; }
    public Integer getSupplierId() { return supplierId; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getNote() { return note; }
    public List<ImportNoteItemResponse> getItems() { return items; }
}