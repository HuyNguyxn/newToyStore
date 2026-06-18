package com.example.new_toy_store.imports.api;

import com.example.new_toy_store.imports.application.ImportService;
import com.example.new_toy_store.imports.application.dto.request.ImportNoteRequest;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/imports")
@PreAuthorize("hasRole('ADMIN')")
public class ImportController {

    private final ImportService service;

    public ImportController(ImportService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ImportNoteResponse> getAll(Pageable pageable) {
        return service.getAllImportNotes(pageable);
    }

    @GetMapping("/{id}")
    public ImportNoteResponse getDetails(@PathVariable Integer id) {
        return service.getImportNoteDetails(id);
    }

    @PostMapping
    public ImportNoteResponse create(@Valid @RequestBody ImportNoteRequest request) {
        return service.createImportNote(request);
    }

    @PatchMapping("/{id}/complete")
    public ImportNoteResponse complete(@PathVariable Integer id) {
        return service.completeImportNote(id);
    }

    @PatchMapping("/{id}/cancel")
    public ImportNoteResponse cancel(@PathVariable Integer id) {
        return service.cancelImportNote(id);
    }
}