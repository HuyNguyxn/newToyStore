package com.example.new_toy_store.imports.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ImportNoteNotFoundException extends ImportDomainException {

    private final Integer importNoteId;

    public ImportNoteNotFoundException(Integer importNoteId) {
        super(
                HttpStatus.NOT_FOUND,
                "IMPORT_NOTE_NOT_FOUND",
                "Không tìm thấy phiếu nhập kho ID " + importNoteId + ".",
                Map.of(
                        "importNoteId", importNoteId,
                        "entity", "ImportNote"
                )
        );
        this.importNoteId = importNoteId;
    }

    public Integer getImportNoteId() {
        return importNoteId;
    }
}
