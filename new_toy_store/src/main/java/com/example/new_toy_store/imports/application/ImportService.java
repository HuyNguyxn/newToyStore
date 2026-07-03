package com.example.new_toy_store.imports.application;

import com.example.new_toy_store.imports.application.dto.request.ImportNoteItemRequest;
import com.example.new_toy_store.imports.application.dto.request.ImportNoteRequest;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import com.example.new_toy_store.imports.domain.ImportNote;
import com.example.new_toy_store.imports.domain.ImportNoteItem;
import com.example.new_toy_store.imports.domain.ImportNoteRepository;
import com.example.new_toy_store.imports.domain.ImportStatus;
import com.example.new_toy_store.imports.domain.exception.ImportNoteNotFoundException;
import com.example.new_toy_store.imports.domain.exception.InvalidImportOperationException;
import com.example.new_toy_store.imports.mapper.ImportNoteMapper;
import com.example.new_toy_store.product.application.ProductService;
import com.example.new_toy_store.product.application.dto.request.ImportedStockRequest;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.supplier.application.SupplierService;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ImportService {

    private final ImportNoteRepository repository;
    private final ProductService productService;
    private final SupplierService supplierService;

    public ImportService(ImportNoteRepository repository, ProductService productService, SupplierService supplierService) {
        this.repository = repository;
        this.productService = productService;
        this.supplierService = supplierService;
    }

    @Transactional(readOnly = true)
    public Page<ImportNoteResponse> searchImportNotes(Integer supplierId, String statusValue, Pageable pageable) {
        ImportStatus status = null;
        if (statusValue != null && !statusValue.trim().isEmpty()) {
            status = ImportStatus.from(statusValue);
        }

        Page<ImportNote> notes = repository.searchImports(supplierId, status, pageable);

        Set<Integer> supplierIds = notes.stream()
                .map(ImportNote::getSupplierId)
                .collect(Collectors.toSet());

        if (supplierIds.isEmpty()) {
            return notes.map(note -> ImportNoteMapper.toFlatResponse(note, null));
        }

        Map<Integer, SupplierResponse> supplierMap = supplierService.getSuppliersByIds(supplierIds)
                .stream()
                .collect(Collectors.toMap(SupplierResponse::getId, s -> s));

        return notes.map(note -> ImportNoteMapper.toFlatResponse(note, supplierMap.get(note.getSupplierId())));
    }

    @Transactional(readOnly = true)
    public ImportNoteResponse getImportNoteDetails(Integer id) {
        ImportNote note = repository.findByIdWithItems(id);
        if (note == null) {
            throw new ImportNoteNotFoundException(id);
        }
        SupplierResponse supplier = supplierService.getSupplierDetails(note.getSupplierId());
        return ImportNoteMapper.toResponse(note, supplier);
    }

    @Transactional
    public ImportNoteResponse createImportNote(ImportNoteRequest request) {
        SupplierResponse supplier = supplierService.getSupplierDetails(request.getSupplierId());
        if (!"ACTIVE".equals(supplier.getStatus())) {
            throw InvalidImportOperationException.supplierInactive(supplier.getStatusDisplayName());
        }

        Set<Integer> productIds = request.getItems().stream()
                .map(ImportNoteItemRequest::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        if (productMap.size() != productIds.size()) {
            throw InvalidImportOperationException.invalidProducts();
        }

        ImportNote note = new ImportNote(request.getSupplierId(), request.getNote());

        for (ImportNoteItemRequest itemReq : request.getItems()) {
            Product product = productMap.get(itemReq.getProductId());
            boolean isValidVariant = product.getVariants().stream()
                    .anyMatch(v -> v.getId().equals(itemReq.getVariantId()));

            if (!isValidVariant) {
                throw InvalidImportOperationException.invalidVariant(itemReq.getVariantId(), product.getName());
            }
            note.addItem(
                    itemReq.getProductId(),
                    itemReq.getVariantId(),
                    itemReq.getProductName(),
                    itemReq.getQuantity(),
                    itemReq.getImportPrice()
            );
        }

        repository.save(note);
        return ImportNoteMapper.toResponse(note, supplier);
    }

    @Transactional
    public ImportNoteResponse completeImportNote(Integer noteId) {
        ImportNote note = repository.findByIdWithItems(noteId);
        if (note == null) {
            throw new ImportNoteNotFoundException(noteId);
        }

        note.complete();
        repository.save(note);

        List<ImportedStockRequest> stockUpdates = note.getItems().stream()
                .map(item -> new ImportedStockRequest(
                        item.getVariantId(),
                        item.getQuantity(),
                        item.getImportPrice()
                ))
                .collect(Collectors.toList());

        productService.processImportedStock(stockUpdates);

        SupplierResponse supplier = supplierService.getSupplierDetails(note.getSupplierId());
        return ImportNoteMapper.toResponse(note, supplier);
    }

    @Transactional
    public ImportNoteResponse cancelImportNote(Integer noteId) {
        ImportNote note = repository.findById(noteId)
                .orElseThrow(() -> new ImportNoteNotFoundException(noteId));
        note.cancel();
        repository.save(note);

        SupplierResponse supplier = supplierService.getSupplierDetails(note.getSupplierId());
        return ImportNoteMapper.toResponse(note, supplier);
    }
}