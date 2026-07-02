package com.example.new_toy_store.imports.mapper;

import com.example.new_toy_store.imports.application.dto.response.ImportNoteItemResponse;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import com.example.new_toy_store.imports.domain.ImportNote;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ImportNoteMapper {

    public static ImportNoteResponse toResponse(ImportNote note, SupplierResponse supplier) {
        List<ImportNoteItemResponse> itemResponses = note.getItems().stream()
                .map(item -> new ImportNoteItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getVariantId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getImportPrice(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());

        return createResponse(note, supplier, itemResponses);
    }
    public static ImportNoteResponse toFlatResponse(ImportNote note, SupplierResponse supplier) {
        return createResponse(note, supplier, Collections.emptyList());
    }

    private static ImportNoteResponse createResponse(ImportNote note, SupplierResponse supplier, List<ImportNoteItemResponse> items) {
        String supplierName = (supplier != null) ? supplier.getName() : "Không xác định";
        String supplierPhone = (supplier != null) ? supplier.getPhoneNumber() : "Không xác định";

        return new ImportNoteResponse(
                note.getId(),
                note.getSupplierId(),
                supplierName,
                supplierPhone,
                note.getStatus().getDisplayName(),
                note.getTotalAmount(),
                note.getNote(),
                items
        );
    }
}