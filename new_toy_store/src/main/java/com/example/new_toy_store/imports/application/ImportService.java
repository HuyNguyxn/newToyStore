package com.example.new_toy_store.imports.application;

import com.example.new_toy_store.global.event.ImportNoteCompletedEvent;
import com.example.new_toy_store.global.event.ImportNoteCompletedItemPayload;
import com.example.new_toy_store.global.event.ImportNoteStatusChangedEvent;
import com.example.new_toy_store.infrastructure.specification.ImportNoteSpecification;
import com.example.new_toy_store.imports.application.dto.request.ImportNoteItemRequest;
import com.example.new_toy_store.imports.application.dto.request.ImportNoteRequest;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import com.example.new_toy_store.imports.domain.ImportNote;
import com.example.new_toy_store.imports.domain.ImportNoteRepository;
import com.example.new_toy_store.imports.domain.ImportStatus;
import com.example.new_toy_store.imports.domain.exception.ImportNoteNotFoundException;
import com.example.new_toy_store.imports.domain.exception.InvalidImportOperationException;
import com.example.new_toy_store.imports.mapper.ImportNoteMapper;
import com.example.new_toy_store.product.application.facade.ProductFacade;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.supplier.application.facade.SupplierFacade;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.domain.SupplierStatus;

import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ImportService {

    private final ImportNoteRepository repository;
    private final ProductFacade productFacade;
    private final SupplierFacade supplierFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    public ImportService(ImportNoteRepository repository,
                         ProductFacade productFacade,
                         SupplierFacade supplierFacade,
                         ApplicationEventPublisher eventPublisher,
                         EntityManager entityManager) {
        this.repository = repository;
        this.productFacade = productFacade;
        this.supplierFacade = supplierFacade;
        this.eventPublisher = eventPublisher;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Page<ImportNoteResponse> searchImportNotes(Integer supplierId, String statusValue, Pageable pageable) {
        ImportStatus status = null;
        if (statusValue != null && !statusValue.trim().isEmpty()) {
            status = ImportStatus.from(statusValue);
        }

        Page<ImportNote> notes = repository.findAll(ImportNoteSpecification.filter(supplierId, status), pageable);

        Set<Integer> supplierIds = notes.stream()
                .map(ImportNote::getSupplierId)
                .collect(Collectors.toSet());

        if (supplierIds.isEmpty()) {
            return notes.map(note -> ImportNoteMapper.toSummaryResponse(note, null));
        }

        Map<Integer, SupplierResponse> supplierMap = supplierFacade.getSuppliersByIds(supplierIds)
                .stream()
                .collect(Collectors.toMap(SupplierResponse::getId, s -> s));

        return notes.map(note -> ImportNoteMapper.toSummaryResponse(note, supplierMap.get(note.getSupplierId())));
    }

    @Transactional(readOnly = true)
    public ImportNoteResponse getImportNoteDetails(Integer id) {
        ImportNote note = repository.findByIdWithItems(id)
                .orElseThrow(() -> new ImportNoteNotFoundException(id));
        SupplierResponse supplier = supplierFacade.getSupplierDetails(note.getSupplierId());
        return ImportNoteMapper.toDetailResponse(note, supplier);
    }

    @Transactional
    public ImportNoteResponse createImportNote(ImportNoteRequest request) {
        SupplierResponse supplier = supplierFacade.getRequiredSupplierDetails(request.getSupplierId(), "imports");
        if (supplier.getStatus() != SupplierStatus.ACTIVE) {
            throw InvalidImportOperationException.supplierInactive(supplier.getStatusDisplayName());
        }

        Set<Integer> productIds = request.getItems().stream()
                .map(ImportNoteItemRequest::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = productFacade.getProductsByIdsWithDetails(productIds)
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
        return ImportNoteMapper.toDetailResponse(note, supplier);
    }

    @Transactional
    public ImportNoteResponse completeImportNote(Integer noteId) {
        ImportNote note = repository.findByIdWithItems(noteId)
                .orElseThrow(() -> new ImportNoteNotFoundException(noteId));

        ImportStatus previousStatus = note.getStatus();
        note.complete();

        List<ImportNoteCompletedItemPayload> completedItems = note.getItems().stream()
                .map(item -> new ImportNoteCompletedItemPayload(
                        item.getVariantId(),
                        item.getQuantity(),
                        item.getImportPrice()
                ))
                .collect(Collectors.toList());

        entityManager.detach(note);
        updateStatusOrFail(noteId, note.getVersion(), previousStatus, note.getStatus());
        publishCompleted(note, completedItems);
        publishStatusChanged(note, previousStatus, note.getStatus());

        SupplierResponse supplier = supplierFacade.getSupplierDetails(note.getSupplierId());
        return ImportNoteMapper.toDetailResponse(note, supplier);
    }

    @Transactional
    public ImportNoteResponse cancelImportNote(Integer noteId) {
        ImportNote note = repository.findById(noteId)
                .orElseThrow(() -> new ImportNoteNotFoundException(noteId));
        ImportStatus previousStatus = note.getStatus();
        note.cancel();
        entityManager.detach(note);
        updateStatusOrFail(noteId, note.getVersion(), previousStatus, note.getStatus());
        publishStatusChanged(note, previousStatus, note.getStatus());

        SupplierResponse supplier = supplierFacade.getSupplierDetails(note.getSupplierId());
        return ImportNoteMapper.toDetailResponse(note, supplier);
    }

    private void updateStatusOrFail(Integer noteId,
                                    Long version,
                                    ImportStatus previousStatus,
                                    ImportStatus nextStatus) {
        int updatedRows = repository.updateStatusWithVersion(noteId, version, previousStatus, nextStatus);
        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(ImportNote.class, noteId);
        }
    }

    private void publishStatusChanged(ImportNote note, ImportStatus previousStatus, ImportStatus nextStatus) {
        eventPublisher.publishEvent(ImportNoteStatusChangedEvent.now(
                note.getId(),
                note.getSupplierId(),
                previousStatus,
                nextStatus
        ));
    }

    private void publishCompleted(ImportNote note, List<ImportNoteCompletedItemPayload> items) {
        eventPublisher.publishEvent(ImportNoteCompletedEvent.now(
                note.getId(),
                note.getSupplierId(),
                items
        ));
    }
}
