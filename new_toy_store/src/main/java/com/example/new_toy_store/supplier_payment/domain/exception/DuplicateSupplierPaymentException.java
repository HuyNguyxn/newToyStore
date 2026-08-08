package com.example.new_toy_store.supplier_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateSupplierPaymentException extends SupplierPaymentDomainException {
    public DuplicateSupplierPaymentException(Integer importNoteId) {
        super(
                HttpStatus.CONFLICT,
                "DUPLICATE_SUPPLIER_PAYMENT",
                "Phiếu nhập này đã có khoản phải trả cho nhà cung cấp.",
                Map.of("importNoteId", importNoteId)
        );
    }
}
