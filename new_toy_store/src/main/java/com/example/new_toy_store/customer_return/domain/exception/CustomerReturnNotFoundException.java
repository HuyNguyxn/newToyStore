package com.example.new_toy_store.customer_return.domain.exception;

public class CustomerReturnNotFoundException extends RuntimeException {
    private final Integer returnId;
    public CustomerReturnNotFoundException(Integer returnId) {
        super("Không tìm thấy Yêu cầu trả hàng với ID: " + returnId);
        this.returnId = returnId; }
    public Integer getReturnId() { return returnId; }
}
