package com.example.new_toy_store.supplier.domain.exception;

public class SupplierNotFoundException extends RuntimeException {
    private final Integer supplierId;

    public SupplierNotFoundException(Integer supplierId) {
        super("Không tìm thấy nhà cung cấp với ID: " + supplierId);
        this.supplierId = supplierId;
    }

    public Integer getSupplierId() { return supplierId; }
}