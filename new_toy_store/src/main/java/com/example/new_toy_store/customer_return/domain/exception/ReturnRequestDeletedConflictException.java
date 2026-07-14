package com.example.new_toy_store.customer_return.domain.exception;

public class ReturnRequestDeletedConflictException extends RuntimeException {
    private final Integer orderId;
    public ReturnRequestDeletedConflictException(Integer orderId) { super("Đơn hàng ID '" + orderId + "' có một yêu cầu trả hàng đã bị xóa. Vui lòng khôi phục lại dữ liệu cũ."); this.orderId = orderId; }
    public Integer getOrderId() { return orderId; }
}