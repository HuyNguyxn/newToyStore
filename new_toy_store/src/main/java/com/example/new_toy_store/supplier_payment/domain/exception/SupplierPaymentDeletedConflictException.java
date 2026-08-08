package com.example.new_toy_store.supplier_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class SupplierPaymentDeletedConflictException extends SupplierPaymentDomainException {
    public SupplierPaymentDeletedConflictException(Integer invoiceId) {
        super(
                HttpStatus.CONFLICT,
                "SUPPLIER_PAYMENT_DELETED_CONFLICT",
                "Khoản thanh toán nhà cung cấp đã bị thay đổi hoặc không còn ở trạng thái có thể hủy.",
                Map.of("invoiceId", invoiceId)
        );
    }
}
