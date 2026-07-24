package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class DuplicateSupplierReturnException extends RuntimeException {

    private final Integer importNoteId;

    public DuplicateSupplierReturnException(Integer importNoteId) {
        super("Phiếu nhập ID " + importNoteId + " đã có một phiếu trả hàng nhà cung cấp đang hoạt động.");
        this.importNoteId = importNoteId;
    }

    public String getErrorType() {
        return "DUPLICATE_ACTIVE_SUPPLIER_RETURN";
    }

    public Map<String, Object> getContextData() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("importNoteId", importNoteId);
        context.put("entity", "SupplierReturn");
        context.put("conflictType", "ACTIVE_DUPLICATE");
        return context;
    }
}
