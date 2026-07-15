package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.HashMap;
import java.util.Map;

public class SupplierReturnNotFoundException extends RuntimeException {

    private final Integer returnId;

    public SupplierReturnNotFoundException(Integer returnId) {
        super("Không tìm thấy Phiếu trả hàng Nhà cung cấp với ID: " + returnId);
        this.returnId = returnId;
    }

    public Map<String, Object> getContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("supplierReturnId", returnId);
        return context;
    }
}