package com.example.new_toy_store.supplier_payment.application.dto.response;

public record SupplierPayableSummary(
        double totalOutstanding,
        double overdueOutstanding,
        long openInvoiceCount,
        long overdueInvoiceCount
) {}
