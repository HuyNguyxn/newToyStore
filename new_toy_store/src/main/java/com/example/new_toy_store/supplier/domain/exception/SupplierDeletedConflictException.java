package com.example.new_toy_store.supplier.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class SupplierDeletedConflictException extends SupplierDomainException {

    private final String phoneNumber;

    public SupplierDeletedConflictException(String phoneNumber) {
        super(
                HttpStatus.CONFLICT,
                "SUPPLIER_SOFT_DELETED_CONFLICT",
                "Số điện thoại '" + phoneNumber + "' thuộc về một nhà cung cấp đã bị xóa mềm. Vui lòng khôi phục thay vì tạo mới.",
                Map.of(
                        "phoneNumber", phoneNumber,
                        "conflictType", "SOFT_DELETED",
                        "suggestedAction", "RESTORE_SUPPLIER"
                )
        );
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
}
