package com.example.new_toy_store.imports.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ImportNoteRequest {
    @NotNull(message = "Supplier ID is required")
    private Integer supplierId;
    private String note;

    @NotEmpty(message = "Items cannot be empty")
    @Valid
    private List<ImportNoteItemRequest> items;

    public Integer getSupplierId() { return supplierId; }
    public String getNote() { return note; }
    public List<ImportNoteItemRequest> getItems() { return items; }
}