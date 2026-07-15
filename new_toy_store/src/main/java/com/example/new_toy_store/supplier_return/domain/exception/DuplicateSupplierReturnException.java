package com.example.new_toy_store.supplier_return.domain.exception;

import java.util.HashMap;
import java.util.Map;

public class DuplicateSupplierReturnException extends RuntimeException {

    private final Integer importNoteId;

    public DuplicateSupplierReturnException(Integer importNoteId) {
        super("Xung đột dữ liệu: Đã tồn tại một Phiếu trả hàng ĐANG HOẠT ĐỘNG cho Phiếu nhập ID: " + importNoteId);
        this.importNoteId = importNoteId;
    }

    public Map<String, Object> getContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("importNoteId", importNoteId);
        return context;
    }
}