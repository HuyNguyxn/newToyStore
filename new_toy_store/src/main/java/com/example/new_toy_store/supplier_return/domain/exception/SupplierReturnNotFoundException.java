package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class SupplierReturnNotFoundException extends RuntimeException {

    private final Integer returnId;

    public SupplierReturnNotFoundException(Integer returnId) {
        super("Không tìm thấy phiếu trả hàng nhà cung cấp với ID: " + returnId + ".");
        this.returnId = returnId;
    }

    public String getErrorType() {
        return "SUPPLIER_RETURN_NOT_FOUND";
    }

    public Map<String, Object> getContextData() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("returnId", returnId);
        context.put("entity", "SupplierReturn");
        return context;
    }
}
