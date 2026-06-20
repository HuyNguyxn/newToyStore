package com.example.new_toy_store.imports.application;

import com.example.new_toy_store.imports.application.dto.request.ImportNoteRequest;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import com.example.new_toy_store.imports.domain.ImportNote;
import com.example.new_toy_store.imports.domain.ImportNoteItem;
import com.example.new_toy_store.imports.domain.ImportNoteRepository;
import com.example.new_toy_store.imports.mapper.ImportNoteMapper;
import com.example.new_toy_store.product.application.ProductService;
import com.example.new_toy_store.supplier.application.SupplierService;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.domain.SupplierStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<ImportNoteResponse> getAllImportNotes(Pageable pageable) {
        Page<ImportNote> notes = repository.findAll(pageable);

        Set<Integer> supplierIds = notes.stream()
                .map(ImportNote::getSupplierId)
                .collect(Collectors.toSet());

        Map<Integer, SupplierResponse> supplierMap = supplierService.getSuppliersByIds(supplierIds)
                .stream()
                .collect(Collectors.toMap(SupplierResponse::getId, s -> s));

        return notes.map(note -> ImportNoteMapper.toResponse(note, supplierMap.get(note.getSupplierId())));
    }

    @Transactional(readOnly = true)
    public ImportNoteResponse getImportNoteDetails(Integer id) {
        ImportNote note = repository.findByIdWithItems(id);
        if (note == null) {
            throw new RuntimeException("Không tìm thấy phiếu nhập kho");
        }
        SupplierResponse supplier = supplierService.getSupplierDetails(note.getSupplierId());
        return ImportNoteMapper.toResponse(note, supplier);
    }

    @Transactional
    public ImportNoteResponse createImportNote(ImportNoteRequest request) {
        SupplierResponse supplier = supplierService.getSupplierDetails(request.getSupplierId());
        if (!SupplierStatus.valueOf(supplier.getStatus()).canImport()) {
            throw new IllegalStateException("Không thể lập phiếu nhập. Nhà cung cấp đang ở trạng thái: " + supplier.getStatus());
        }

        ImportNote note = new ImportNote(request.getSupplierId(), request.getNote());
        request.getItems().forEach(itemReq -> {
            note.addItem(
                    itemReq.getProductId(),
                    itemReq.getVariantId(),
                    itemReq.getProductName(),
                    itemReq.getQuantity(),
                    itemReq.getImportPrice()
            );
        });
        repository.save(note);
        return ImportNoteMapper.toResponse(note, supplier);
    }

    @Transactional
    public ImportNoteResponse completeImportNote(Integer noteId) {
        ImportNote note = repository.findByIdWithItems(noteId);
        if (note == null) {
            throw new RuntimeException("Không tìm thấy phiếu nhập kho");
        }

        note.complete();
        repository.save(note);

        Map<Integer, Integer> variantQuantities = note.getItems().stream()
                .collect(Collectors.toMap(
                        ImportNoteItem::getVariantId,
                        ImportNoteItem::getQuantity,
                        Integer::sum
                ));
        productService.addStockFromImport(variantQuantities);

        SupplierResponse supplier = supplierService.getSupplierDetails(note.getSupplierId());
        return ImportNoteMapper.toResponse(note, supplier);
    }

    @Transactional
    public ImportNoteResponse cancelImportNote(Integer noteId) {
        ImportNote note = repository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập kho"));
        note.cancel();
        repository.save(note);

        SupplierResponse supplier = supplierService.getSupplierDetails(note.getSupplierId());
        return ImportNoteMapper.toResponse(note, supplier);
    }
}