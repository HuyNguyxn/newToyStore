package com.example.new_toy_store.customer_return.domain.exception;

public class DuplicateReturnRequestException extends RuntimeException {
    private final Integer orderId;
    public DuplicateReturnRequestException(Integer orderId) { super("Đơn hàng ID '" + orderId + "' đã có một yêu cầu trả hàng đang được xử lý."); this.orderId = orderId; }
    public Integer getOrderId() { return orderId; }
}