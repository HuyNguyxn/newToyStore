package com.example.new_toy_store.supplier.api;

import com.example.new_toy_store.supplier.application.SupplierService;
import com.example.new_toy_store.supplier.application.dto.request.SupplierCreateRequest;
import com.example.new_toy_store.supplier.application.dto.request.SupplierFilterRequest;
import com.example.new_toy_store.supplier.application.dto.request.SupplierStatusChangeRequest;
import com.example.new_toy_store.supplier.application.dto.request.SupplierUpdateRequest;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
@PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
@Validated
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @GetMapping
    public Page<SupplierResponse> getAll(
            @ModelAttribute SupplierFilterRequest filterRequest,
            Pageable pageable) {
        return service.filterSuppliers(filterRequest, pageable);
    }

    @GetMapping("/{id}")
    public SupplierResponse getDetails(@PathVariable Integer id) {
        return service.getSupplierDetails(id);
    }

    @PostMapping
    public SupplierResponse create(@Valid @RequestBody SupplierCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable Integer id, @Valid @RequestBody SupplierUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/status")
    public void changeStatus(@PathVariable Integer id, @Valid @RequestBody SupplierStatusChangeRequest request) {
        service.changeStatus(id, request.getStatus());
    }

    @PatchMapping("/{id}/restore")
    public void restore(@PathVariable Integer id) {
        service.restore(id);
    }
}
