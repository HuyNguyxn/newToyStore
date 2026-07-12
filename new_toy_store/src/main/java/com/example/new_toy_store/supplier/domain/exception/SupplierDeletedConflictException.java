package com.example.new_toy_store.supplier.domain.exception;

public class SupplierDeletedConflictException extends RuntimeException {
    private final String phoneNumber;

    public SupplierDeletedConflictException(String phoneNumber) {
        super("Số điện thoại '" + phoneNumber + "' thuộc về một nhà cung cấp đã bị xóa. Vui lòng khôi phục lại thay vì tạo mới.");
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
}