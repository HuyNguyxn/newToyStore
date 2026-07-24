package com.example.new_toy_store.imports.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ImportDeletedConflictException extends ImportDomainException {

    public ImportDeletedConflictException(Integer importNoteId, String action) {
        super(
                HttpStatus.CONFLICT,
                "IMPORT_NOTE_DELETED_CONFLICT",
                "Phiếu nhập ID " + importNoteId + " đã bị xóa mềm nên không thể " + action + ".",
                Map.of(
                        "importNoteId", importNoteId,
                        "action", action,
                        "reason", "SOFT_DELETED"
                )
        );
    }
}
