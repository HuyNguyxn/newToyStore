package com.example.new_toy_store.supplier.domain.exception;

public class SupplierAccessDeniedException extends RuntimeException {
    private final String action;

    public SupplierAccessDeniedException(String action) {
        super("Bạn không có quyền thực hiện hành động này trên Nhà cung cấp: " + action);
        this.action = action;
    }

    public String getAction() { return action; }
}