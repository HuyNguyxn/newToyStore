package com.example.new_toy_store.warehouse.application;

import com.example.new_toy_store.imports.application.ImportService;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import com.example.new_toy_store.imports.domain.ImportStatus;
import com.example.new_toy_store.imports.domain.exception.ImportCrossModuleException;
import com.example.new_toy_store.imports.domain.exception.InvalidImportOperationException;
import com.example.new_toy_store.product.application.dto.request.UpdateProductStatusRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import com.example.new_toy_store.product.application.facade.ProductFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseService {

    private final ImportService importService;
    private final ProductFacade productFacade;

    public WarehouseService(ImportService importService, ProductFacade productFacade) {
        this.importService = importService;
        this.productFacade = productFacade;
    }

    @Transactional(readOnly = true)
    public Page<ImportNoteResponse> getBatches(Integer supplierId, String status, String keyword, Pageable pageable) {
        return importService.searchImportNotes(supplierId, status, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public ImportNoteResponse getBatchDetails(Integer batchId) {
        return importService.getImportNoteDetails(batchId);
    }

    /**
     * Confirming a batch is the single warehouse action that makes its stock
     * available. ImportService publishes the completion event, and the product
     * module consumes that event in the same transaction to update inventory.
     */
    @Transactional
    public ImportNoteResponse completeBatch(Integer batchId) {
        return importService.completeImportNote(batchId);
    }

    /**
     * A draft/counting batch can be cancelled, but completed stock is never
     * silently reversed from this screen.
     */
    @Transactional
    public ImportNoteResponse cancelBatch(Integer batchId) {
        return importService.cancelImportNote(batchId);
    }

    @Transactional
    public ProductResponse publishProduct(Integer batchId, Integer productId) {
        ImportNoteResponse batch = importService.getImportNoteDetails(batchId);
        ImportStatus status = batch.getStatus();
        if (status != ImportStatus.COMPLETED) {
            throw InvalidImportOperationException.invalidStatusTransition("đưa sản phẩm lên cửa hàng");
        }

        boolean belongsToBatch = batch.getItems().stream()
                .anyMatch(item -> productId.equals(item.getProductId()));
        if (!belongsToBatch) {
            throw ImportCrossModuleException.invalidProduct(productId, null, "PRODUCT_NOT_IN_IMPORT_BATCH");
        }

        UpdateProductStatusRequest request = new UpdateProductStatusRequest("ACTIVE");
        return productFacade.updateStatus(productId, request);
    }
}
