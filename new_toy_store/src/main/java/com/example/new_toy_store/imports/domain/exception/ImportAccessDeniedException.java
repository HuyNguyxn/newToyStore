package com.example.new_toy_store.imports.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ImportAccessDeniedException extends ImportDomainException {

    public ImportAccessDeniedException(Integer importNoteId, Integer currentUserId, String action) {
        super(
                HttpStatus.FORBIDDEN,
                "IMPORT_ACCESS_DENIED",
                "Người dùng ID " + currentUserId + " không có quyền " + action + " phiếu nhập ID " + importNoteId + ".",
                Map.of(
                        "importNoteId", importNoteId,
                        "currentUserId", currentUserId,
                        "action", action
                )
        );
    }
}
