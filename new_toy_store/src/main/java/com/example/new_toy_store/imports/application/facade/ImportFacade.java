package com.example.new_toy_store.imports.application.facade;

import com.example.new_toy_store.imports.application.ImportService;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ImportFacade {

    private final ImportService importService;

    public ImportFacade(ImportService importService) {
        this.importService = importService;
    }

    public ImportNoteResponse getImportNoteDetails(Integer importNoteId) {
        return importService.getImportNoteDetails(importNoteId);
    }

    public Page<ImportNoteResponse> searchImportNotes(
            Integer supplierId,
            String status,
            String keyword,
            Pageable pageable
    ) {
        return importService.searchImportNotes(supplierId, status, keyword, pageable);
    }

    public ImportNoteResponse completeImportNote(Integer importNoteId) {
        return importService.completeImportNote(importNoteId);
    }

    public ImportNoteResponse cancelImportNote(Integer importNoteId) {
        return importService.cancelImportNote(importNoteId);
    }
}
