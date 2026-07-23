package com.example.new_toy_store.supplier.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class SupplierAccessDeniedException extends SupplierDomainException {

    private final String action;

    public SupplierAccessDeniedException(String action) {
        super(
                HttpStatus.FORBIDDEN,
                "SUPPLIER_ACCESS_DENIED",
                "Bạn không có quyền thực hiện hành động này trên nhà cung cấp: " + action,
                Map.of(
                        "deniedAction", action,
                        "suggestedAction", "CONTACT_ADMIN"
                )
        );
        this.action = action;
    }

    public String getAction() { return action; }
}
