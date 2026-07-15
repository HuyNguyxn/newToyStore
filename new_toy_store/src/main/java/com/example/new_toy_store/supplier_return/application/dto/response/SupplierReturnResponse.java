package com.example.new_toy_store.supplier_return.application.dto.response;

import java.util.List;

public class SupplierReturnResponse {

    private Integer id;
    private Integer supplierId;
    private Integer importNoteId;
    private String status;
    private String statusDisplayName;
    private double freightCost;
    private double restockingFee;
    private double totalRefundAmount;
    private String note;
    private List<String> availableNextActions;
    private List<SupplierReturnItemResponse> items;
    private List<SupplierReturnHistoryResponse> histories;
    private List<SupplierReturnImageResponse> images;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Integer getImportNoteId() {
        return importNoteId;
    }

    public void setImportNoteId(Integer importNoteId) {
        this.importNoteId = importNoteId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }

    public double getFreightCost() {
        return freightCost;
    }

    public void setFreightCost(double freightCost) {
        this.freightCost = freightCost;
    }

    public double getRestockingFee() {
        return restockingFee;
    }

    public void setRestockingFee(double restockingFee) {
        this.restockingFee = restockingFee;
    }

    public double getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public void setTotalRefundAmount(double totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getAvailableNextActions() {
        return availableNextActions;
    }

    public void setAvailableNextActions(List<String> availableNextActions) {
        this.availableNextActions = availableNextActions;
    }

    public List<SupplierReturnItemResponse> getItems() {
        return items;
    }

    public void setItems(List<SupplierReturnItemResponse> items) {
        this.items = items;
    }
    public List<SupplierReturnHistoryResponse> getHistories() { return histories; }
    public void setHistories(List<SupplierReturnHistoryResponse> histories) { this.histories = histories; }

    public List<SupplierReturnImageResponse> getImages() { return images; }
    public void setImages(List<SupplierReturnImageResponse> images) { this.images = images; }
}