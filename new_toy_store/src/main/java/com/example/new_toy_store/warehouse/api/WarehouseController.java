package com.example.new_toy_store.warehouse.api;

import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import com.example.new_toy_store.warehouse.application.WarehouseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/warehouse/batches")
@PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
@Validated
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public Page<ImportNoteResponse> getBatches(
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return warehouseService.getBatches(supplierId, status, keyword, pageable);
    }

    @GetMapping("/{batchId}")
    public ImportNoteResponse getBatchDetails(@PathVariable Integer batchId) {
        return warehouseService.getBatchDetails(batchId);
    }

    @PatchMapping("/{batchId}/complete")
    public ImportNoteResponse completeBatch(@PathVariable Integer batchId) {
        return warehouseService.completeBatch(batchId);
    }

    @PatchMapping("/{batchId}/cancel")
    public ImportNoteResponse cancelBatch(@PathVariable Integer batchId) {
        return warehouseService.cancelBatch(batchId);
    }

    @PatchMapping("/{batchId}/reconcile")
    public ImportNoteResponse reconcileBatch(@PathVariable Integer batchId) {
        return warehouseService.reconcileBatch(batchId);
    }

    @PatchMapping("/{batchId}/products/{productId}/publish")
    public ProductResponse publishProduct(
            @PathVariable Integer batchId,
            @PathVariable Integer productId
    ) {
        return warehouseService.publishProduct(batchId, productId);
    }
}
