package com.example.new_toy_store.supplier_payment.application.listener;

import com.example.new_toy_store.global.event.ImportNoteCompletedEvent;
import com.example.new_toy_store.supplier_payment.application.facade.SupplierPaymentFacade;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SupplierPaymentImportListener {

    private final SupplierPaymentFacade supplierPaymentFacade;

    public SupplierPaymentImportListener(SupplierPaymentFacade supplierPaymentFacade) {
        this.supplierPaymentFacade = supplierPaymentFacade;
    }

    @EventListener
    public void createPayableWhenImportCompleted(ImportNoteCompletedEvent event) {
        supplierPaymentFacade.createFromCompletedImportIfMissing(event.importNoteId());
    }
}
