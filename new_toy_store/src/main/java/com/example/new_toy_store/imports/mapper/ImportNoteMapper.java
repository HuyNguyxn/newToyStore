package com.example.new_toy_store.imports.mapper;

import com.example.new_toy_store.imports.application.dto.response.ImportNoteItemResponse;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import com.example.new_toy_store.imports.application.dto.response.ImportStatusActionResponse;
import com.example.new_toy_store.imports.domain.ImportNote;
import com.example.new_toy_store.imports.domain.ImportStatus;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ImportNoteMapper {

    private static final String UNKNOWN_SUPPLIER = "Không xác định";

    private ImportNoteMapper() {
    }

    public static ImportNoteResponse toResponse(ImportNote note, SupplierResponse supplier) {
        return toDetailResponse(note, supplier);
    }

    public static ImportNoteResponse toFlatResponse(ImportNote note, SupplierResponse supplier) {
        return toSummaryResponse(note, supplier);
    }

    public static ImportNoteResponse toDetailResponse(ImportNote note, SupplierResponse supplier) {
        return createImportNoteResponse(note, supplier, mapItems(note));
    }

    public static ImportNoteResponse toSummaryResponse(ImportNote note, SupplierResponse supplier) {
        return createImportNoteResponse(note, supplier, Collections.emptyList());
    }

    private static List<ImportNoteItemResponse> mapItems(ImportNote note) {
        return note.getItems().stream()
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
    }

    private static ImportNoteResponse createImportNoteResponse(ImportNote note,
                                                               SupplierResponse supplier,
                                                               List<ImportNoteItemResponse> items) {
        return new ImportNoteResponse(
                note.getId(),
                note.getSupplierId(),
                resolveSupplierName(supplier),
                resolveSupplierPhone(supplier),
                note.getStatus(),
                mapAllowedNextActions(note.getStatus()),
                note.getTotalAmount(),
                note.getNote(),
                items
        );
    }

    private static List<ImportStatusActionResponse> mapAllowedNextActions(ImportStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }

        return status.getAllowedNextStatuses().stream()
                .map(ImportNoteMapper::mapStatusAction)
                .toList();
    }

    private static ImportStatusActionResponse mapStatusAction(String targetStatus) {
        if (ImportStatus.COMPLETED.getCode().equals(targetStatus)) {
            return new ImportStatusActionResponse("COMPLETE", targetStatus, "Hoàn tất phiếu nhập");
        }
        if (ImportStatus.CANCELLED.getCode().equals(targetStatus)) {
            return new ImportStatusActionResponse("CANCEL", targetStatus, "Hủy phiếu nhập");
        }
        return new ImportStatusActionResponse("CHANGE_STATUS", targetStatus, "Chuyển trạng thái");
    }

    private static String resolveSupplierName(SupplierResponse supplier) {
        return supplier != null ? supplier.getName() : UNKNOWN_SUPPLIER;
    }

    private static String resolveSupplierPhone(SupplierResponse supplier) {
        return supplier != null ? supplier.getPhoneNumber() : UNKNOWN_SUPPLIER;
    }
}
