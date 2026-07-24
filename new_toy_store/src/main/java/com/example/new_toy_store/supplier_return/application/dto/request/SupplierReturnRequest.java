package com.example.new_toy_store.supplier_return.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SupplierReturnRequest {

    @NotNull(message = "Mã nhà cung cấp không được để trống")
    private Integer supplierId;

    private Integer importNoteId;

    @Min(value = 0, message = "Phí vận chuyển không được âm")
    private double freightCost;

    @Min(value = 0, message = "Phí lưu kho không được âm")
    private double restockingFee;

    private String note;

    @NotEmpty(message = "Danh sách sản phẩm không được rỗng")
    @Valid
    private List<SupplierReturnItemRequest> items;

    private List<String> imageUrls;

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }
    public Integer getImportNoteId() { return importNoteId; }
    public void setImportNoteId(Integer importNoteId) { this.importNoteId = importNoteId; }
    public double getFreightCost() { return freightCost; }
    public void setFreightCost(double freightCost) { this.freightCost = freightCost; }
    public double getRestockingFee() { return restockingFee; }
    public void setRestockingFee(double restockingFee) { this.restockingFee = restockingFee; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<SupplierReturnItemRequest> getItems() { return items; }
    public void setItems(List<SupplierReturnItemRequest> items) { this.items = items; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
