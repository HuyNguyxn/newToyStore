package com.example.new_toy_store.imports.application.facade;

import com.example.new_toy_store.imports.application.ImportService;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
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
}
