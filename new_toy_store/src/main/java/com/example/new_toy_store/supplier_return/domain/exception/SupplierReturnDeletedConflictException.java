package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.HashMap;
import java.util.Map;

public class SupplierReturnDeletedConflictException extends RuntimeException {

    private final Integer supplierReturnId;

    public SupplierReturnDeletedConflictException(Integer supplierReturnId) {
        super("Xung đột dữ liệu: Phiếu trả hàng ID " + supplierReturnId + " đã bị XÓA MỀM (Soft-deleted) trong hệ thống.");
        this.supplierReturnId = supplierReturnId;
    }

    public Map<String, Object> getContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("supplierReturnId", supplierReturnId);
        return context;
    }
}