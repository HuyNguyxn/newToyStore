package com.example.new_toy_store.supplier.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class SupplierNotFoundException extends SupplierDomainException {

    private final Integer supplierId;

    public SupplierNotFoundException(Integer supplierId) {
        super(
                HttpStatus.NOT_FOUND,
                "SUPPLIER_NOT_FOUND",
                "Không tìm thấy nhà cung cấp với ID: " + supplierId,
                Map.of("supplierId", supplierId)
        );
        this.supplierId = supplierId;
    }

    public Integer getSupplierId() { return supplierId; }
}
