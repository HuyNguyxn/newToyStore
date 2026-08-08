package com.example.new_toy_store.supplier_payment.application.service;

import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import com.example.new_toy_store.imports.application.facade.ImportFacade;
import com.example.new_toy_store.imports.domain.ImportStatus;
import com.example.new_toy_store.infrastructure.specification.SupplierPaymentSpecification;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.application.facade.SupplierFacade;
import com.example.new_toy_store.supplier_payment.application.dto.request.SupplierPaymentCancelRequest;
import com.example.new_toy_store.supplier_payment.application.dto.request.SupplierPaymentFilterRequest;
import com.example.new_toy_store.supplier_payment.application.dto.request.SupplierPaymentRecordRequest;
import com.example.new_toy_store.supplier_payment.application.dto.response.SupplierPaymentResponse;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentInvoice;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentRepository;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentStatus;
import com.example.new_toy_store.supplier_payment.domain.exception.DuplicateSupplierPaymentException;
import com.example.new_toy_store.supplier_payment.domain.exception.InvalidSupplierPaymentOperationException;
import com.example.new_toy_store.supplier_payment.domain.exception.SupplierPaymentDeletedConflictException;
import com.example.new_toy_store.supplier_payment.domain.exception.SupplierPaymentNotFoundException;
import com.example.new_toy_store.supplier_payment.mapper.SupplierPaymentMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SupplierPaymentService {

    private static final int DEFAULT_DUE_DAYS = 30;

    private final SupplierPaymentRepository repository;
    private final SupplierFacade supplierFacade;
    private final ImportFacade importFacade;

    public SupplierPaymentService(SupplierPaymentRepository repository, SupplierFacade supplierFacade, ImportFacade importFacade) {
        this.repository = repository;
        this.supplierFacade = supplierFacade;
        this.importFacade = importFacade;
    }

    @Transactional(readOnly = true)
    public Page<SupplierPaymentResponse> filter(SupplierPaymentFilterRequest request, Pageable pageable) {
        Page<SupplierPaymentInvoice> invoices = repository.findAll(SupplierPaymentSpecification.filter(request), pageable);

        Set<Integer> supplierIds = invoices.stream()
                .map(SupplierPaymentInvoice::getSupplierId)
                .collect(Collectors.toSet());

        Map<Integer, SupplierResponse> supplierMap = supplierFacade.getSuppliersByIds(supplierIds)
                .stream()
                .collect(Collectors.toMap(SupplierResponse::getId, supplier -> supplier));

        return invoices.map(invoice -> SupplierPaymentMapper.toSummaryResponse(invoice, supplierMap.get(invoice.getSupplierId())));
    }

    @Transactional(readOnly = true)
    public SupplierPaymentResponse getDetails(Integer invoiceId) {
        SupplierPaymentInvoice invoice = repository.findByIdWithTransactions(invoiceId)
                .orElseThrow(() -> new SupplierPaymentNotFoundException(invoiceId));
        SupplierResponse supplier = supplierFacade.getSupplierDetails(invoice.getSupplierId());
        return SupplierPaymentMapper.toDetailResponse(invoice, supplier);
    }

    @Transactional
    public SupplierPaymentResponse createFromImportNote(Integer importNoteId) {
        if (repository.existsByImportNoteId(importNoteId)) {
            throw new DuplicateSupplierPaymentException(importNoteId);
        }

        ImportNoteResponse importNote = importFacade.getImportNoteDetails(importNoteId);
        if (importNote.getStatus() != ImportStatus.COMPLETED) {
            throw InvalidSupplierPaymentOperationException.importNoteNotCompleted(importNote.getId(), importNote.getStatus().name());
        }

        SupplierPaymentInvoice invoice = new SupplierPaymentInvoice(
                importNote.getSupplierId(),
                importNote.getId(),
                buildInvoiceCode(importNote.getId()),
                importNote.getTotalAmount(),
                LocalDate.now().plusDays(DEFAULT_DUE_DAYS),
                "Tạo công nợ từ phiếu nhập #" + importNote.getId()
        );

        repository.save(invoice);
        SupplierResponse supplier = supplierFacade.getSupplierDetails(invoice.getSupplierId());
        return SupplierPaymentMapper.toDetailResponse(invoice, supplier);
    }

    @Transactional
    public SupplierPaymentResponse createFromCompletedImportIfMissing(Integer importNoteId) {
        return repository.findByImportNoteId(importNoteId)
                .map(invoice -> {
                    SupplierResponse supplier = supplierFacade.getSupplierDetails(invoice.getSupplierId());
                    return SupplierPaymentMapper.toDetailResponse(invoice, supplier);
                })
                .orElseGet(() -> createFromImportNote(importNoteId));
    }

    @Transactional
    public SupplierPaymentResponse recordPayment(Integer invoiceId, SupplierPaymentRecordRequest request) {
        SupplierPaymentInvoice invoice = repository.findByIdWithTransactions(invoiceId)
                .orElseThrow(() -> new SupplierPaymentNotFoundException(invoiceId));
        invoice.markOverdue(LocalDate.now());
        invoice.recordPayment(
                request.getAmount(),
                request.getMethod(),
                request.getReferenceCode(),
                request.getPaidDate(),
                request.getNote()
        );
        repository.save(invoice);
        SupplierResponse supplier = supplierFacade.getSupplierDetails(invoice.getSupplierId());
        return SupplierPaymentMapper.toDetailResponse(invoice, supplier);
    }

    @Transactional
    public SupplierPaymentResponse cancel(Integer invoiceId, SupplierPaymentCancelRequest request) {
        SupplierPaymentInvoice invoice = repository.findById(invoiceId)
                .orElseThrow(() -> new SupplierPaymentNotFoundException(invoiceId));
        if (!invoice.getStatus().canTransitionTo(SupplierPaymentStatus.CANCELLED)) {
            throw InvalidSupplierPaymentOperationException.invalidTransition(invoice.getStatus(), SupplierPaymentStatus.CANCELLED);
        }

        int updatedRows = repository.cancelWithVersion(
                invoice.getId(),
                invoice.getVersion(),
                SupplierPaymentStatus.CANCELLED,
                request == null ? null : request.getReason()
        );

        if (updatedRows == 0) {
            throw new SupplierPaymentDeletedConflictException(invoiceId);
        }

        return getDetails(invoiceId);
    }

    private String buildInvoiceCode(Integer importNoteId) {
        return "SPAY-IMP-" + String.format("%06d", importNoteId);
    }
}
