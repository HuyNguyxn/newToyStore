package com.example.new_toy_store.imports.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ImportNoteRequest {
    @NotNull(message = "Mã nhà cung cấp không được để trống")
    private Integer supplierId;

    private String note;

    @NotEmpty(message = "Danh sách sản phẩm nhập không được để trống")
    @Valid
    private List<ImportNoteItemRequest> items;

    public Integer getSupplierId() { return supplierId; }
    public String getNote() { return note; }
    public List<ImportNoteItemRequest> getItems() { return items; }
}