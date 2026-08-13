package com.example.new_toy_store.global.event;

import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentMethod;

import java.time.Instant;
import java.time.LocalDate;

public record SupplierPaymentRecordedEvent(
        Integer transactionId,
        Integer invoiceId,
        Integer supplierId,
        Integer importNoteId,
        double amount,
        SupplierPaymentMethod method,
        String referenceCode,
        LocalDate paidDate,
        Instant occurredAt
) {
    public static SupplierPaymentRecordedEvent now(
            Integer transactionId,
            Integer invoiceId,
            Integer supplierId,
            Integer importNoteId,
            double amount,
            SupplierPaymentMethod method,
            String referenceCode,
            LocalDate paidDate
    ) {
        return new SupplierPaymentRecordedEvent(
                transactionId, invoiceId, supplierId, importNoteId, amount,
                method, referenceCode, paidDate, Instant.now()
        );
    }
}
