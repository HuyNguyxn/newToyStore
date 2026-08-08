package com.example.new_toy_store.supplier_payment.mapper;

import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier_payment.application.dto.response.SupplierPaymentActionResponse;
import com.example.new_toy_store.supplier_payment.application.dto.response.SupplierPaymentResponse;
import com.example.new_toy_store.supplier_payment.application.dto.response.SupplierPaymentTransactionResponse;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentInvoice;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentTransaction;

import java.util.List;

public final class SupplierPaymentMapper {

    private SupplierPaymentMapper() {
    }

    public static SupplierPaymentResponse toSummaryResponse(SupplierPaymentInvoice invoice, SupplierResponse supplier) {
        return toResponse(invoice, supplier, List.of());
    }

    public static SupplierPaymentResponse toDetailResponse(SupplierPaymentInvoice invoice, SupplierResponse supplier) {
        List<SupplierPaymentTransactionResponse> transactions = invoice.getTransactions().stream()
                .map(SupplierPaymentMapper::toTransactionResponse)
                .toList();
        return toResponse(invoice, supplier, transactions);
    }

    private static SupplierPaymentResponse toResponse(
            SupplierPaymentInvoice invoice,
            SupplierResponse supplier,
            List<SupplierPaymentTransactionResponse> transactions
    ) {
        return new SupplierPaymentResponse(
                invoice.getId(),
                invoice.getSupplierId(),
                supplier == null ? null : supplier.getName(),
                invoice.getImportNoteId(),
                invoice.getInvoiceCode(),
                invoice.getStatus(),
                invoice.getStatus().getNextValidStates().stream().map(SupplierPaymentActionResponse::new).toList(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getRemainingAmount(),
                invoice.getDueDate(),
                invoice.getNote(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt(),
                transactions
        );
    }

    private static SupplierPaymentTransactionResponse toTransactionResponse(SupplierPaymentTransaction transaction) {
        return new SupplierPaymentTransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getMethod(),
                transaction.getReferenceCode(),
                transaction.getPaidDate(),
                transaction.getNote(),
                transaction.getCreatedAt()
        );
    }
}
