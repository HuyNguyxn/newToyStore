package com.example.new_toy_store.supplier_payment.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class SupplierPaymentNotFoundException extends SupplierPaymentDomainException {
    public SupplierPaymentNotFoundException(Integer invoiceId) {
        super(
                HttpStatus.NOT_FOUND,
                "SUPPLIER_PAYMENT_NOT_FOUND",
                "Không tìm thấy khoản thanh toán nhà cung cấp.",
                Map.of("invoiceId", invoiceId)
        );
    }
}
