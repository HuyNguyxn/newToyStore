package com.example.new_toy_store.supplier_payment.application.facade;

import com.example.new_toy_store.supplier_payment.application.dto.response.SupplierPaymentResponse;
import com.example.new_toy_store.supplier_payment.application.dto.response.SupplierPayableSummary;
import com.example.new_toy_store.supplier_payment.application.service.SupplierPaymentService;
import org.springframework.stereotype.Component;

@Component
public class SupplierPaymentFacade {

    private final SupplierPaymentService supplierPaymentService;

    public SupplierPaymentFacade(SupplierPaymentService supplierPaymentService) {
        this.supplierPaymentService = supplierPaymentService;
    }

    public SupplierPaymentResponse createFromCompletedImportIfMissing(Integer importNoteId) {
        return supplierPaymentService.createFromCompletedImportIfMissing(importNoteId);
    }

    public SupplierPayableSummary getPayableSummary() {
        return supplierPaymentService.getPayableSummary();
    }
}
