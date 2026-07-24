package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class SupplierReturnDeletedConflictException extends RuntimeException {

    private final Integer supplierReturnId;

    public SupplierReturnDeletedConflictException(Integer supplierReturnId) {
        super("Phiếu trả hàng nhà cung cấp ID " + supplierReturnId + " đã bị xóa mềm. Vui lòng khôi phục bản ghi cũ thay vì tạo mới.");
        this.supplierReturnId = supplierReturnId;
    }

    public String getErrorType() {
        return "SOFT_DELETED_SUPPLIER_RETURN_CONFLICT";
    }

    public Map<String, Object> getContextData() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("supplierReturnId", supplierReturnId);
        context.put("entity", "SupplierReturn");
        context.put("conflictType", "SOFT_DELETED_CONFLICT");
        context.put("suggestedAction", "RESTORE_DELETED_RECORD");
        return context;
    }
}
