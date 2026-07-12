package com.example.new_toy_store.supplier.domain.exception;

public class DuplicateSupplierException extends RuntimeException {
    private final String phoneNumber;

    public DuplicateSupplierException(String phoneNumber) {
        super("Số điện thoại '" + phoneNumber + "' đã được sử dụng bởi một nhà cung cấp khác đang hoạt động.");
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
}