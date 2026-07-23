package com.example.new_toy_store.supplier.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateSupplierException extends SupplierDomainException {

    private final String phoneNumber;

    public DuplicateSupplierException(String phoneNumber) {
        super(
                HttpStatus.CONFLICT,
                "SUPPLIER_ACTIVE_DUPLICATE",
                "Số điện thoại '" + phoneNumber + "' đã được sử dụng bởi một nhà cung cấp đang hoạt động.",
                Map.of(
                        "phoneNumber", phoneNumber,
                        "conflictType", "ACTIVE",
                        "suggestedAction", "USE_DIFFERENT_PHONE_NUMBER"
                )
        );
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
}
