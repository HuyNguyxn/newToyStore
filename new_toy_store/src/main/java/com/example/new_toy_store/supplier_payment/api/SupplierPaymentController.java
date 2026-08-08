package com.example.new_toy_store.supplier_payment.api;

import com.example.new_toy_store.supplier_payment.application.dto.request.SupplierPaymentCancelRequest;
import com.example.new_toy_store.supplier_payment.application.dto.request.SupplierPaymentFilterRequest;
import com.example.new_toy_store.supplier_payment.application.dto.request.SupplierPaymentRecordRequest;
import com.example.new_toy_store.supplier_payment.application.dto.response.SupplierPaymentResponse;
import com.example.new_toy_store.supplier_payment.application.service.SupplierPaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier-payments")
@Validated
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class SupplierPaymentController {

    private final SupplierPaymentService service;

    public SupplierPaymentController(SupplierPaymentService service) {
        this.service = service;
    }

    @GetMapping
    public Page<SupplierPaymentResponse> filter(@ModelAttribute SupplierPaymentFilterRequest request, Pageable pageable) {
        return service.filter(request, pageable);
    }

    @GetMapping("/{id}")
    public SupplierPaymentResponse getDetails(@PathVariable Integer id) {
        return service.getDetails(id);
    }

    @PostMapping("/imports/{importNoteId}")
    public SupplierPaymentResponse createFromImportNote(@PathVariable Integer importNoteId) {
        return service.createFromImportNote(importNoteId);
    }

    @PatchMapping("/{id}/payments")
    public SupplierPaymentResponse recordPayment(
            @PathVariable Integer id,
            @Valid @RequestBody SupplierPaymentRecordRequest request
    ) {
        return service.recordPayment(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public SupplierPaymentResponse cancel(
            @PathVariable Integer id,
            @Valid @RequestBody(required = false) SupplierPaymentCancelRequest request
    ) {
        return service.cancel(id, request);
    }
}
