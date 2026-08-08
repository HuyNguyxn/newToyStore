package com.example.new_toy_store.supplier_payment.application.dto.response;

import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentStatus;

public class SupplierPaymentActionResponse {
    private final String code;
    private final String label;
    private final String description;

    public SupplierPaymentActionResponse(SupplierPaymentStatus status) {
        this.code = status.getCode();
        this.label = status.getDisplayName();
        this.description = status.getDescription();
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
}
