package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.supplier_payment.application.dto.request.SupplierPaymentFilterRequest;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentInvoice;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentStatus;
import org.springframework.data.jpa.domain.Specification;

public final class SupplierPaymentSpecification {

    private SupplierPaymentSpecification() {
    }

    public static Specification<SupplierPaymentInvoice> filter(SupplierPaymentFilterRequest request) {
        return Specification
                .where(hasSupplierId(request.getSupplierId()))
                .and(hasImportNoteId(request.getImportNoteId()))
                .and(hasStatus(request.getStatus()));
    }

    private static Specification<SupplierPaymentInvoice> hasSupplierId(Integer supplierId) {
        return (root, query, cb) -> supplierId == null ? cb.conjunction() : cb.equal(root.get("supplierId"), supplierId);
    }

    private static Specification<SupplierPaymentInvoice> hasImportNoteId(Integer importNoteId) {
        return (root, query, cb) -> importNoteId == null ? cb.conjunction() : cb.equal(root.get("importNoteId"), importNoteId);
    }

    private static Specification<SupplierPaymentInvoice> hasStatus(String statusValue) {
        return (root, query, cb) -> {
            if (statusValue == null || statusValue.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), SupplierPaymentStatus.from(statusValue));
        };
    }
}
