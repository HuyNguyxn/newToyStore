package com.example.new_toy_store.order.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class OrderDeletedConflictException extends OrderDomainException {

    public OrderDeletedConflictException(Integer orderId) {
        super(
                HttpStatus.CONFLICT,
                "ORDER_DELETED_CONFLICT",
                "Đơn hàng có ID " + orderId + " đã bị xóa mềm nên không thể tiếp tục thao tác.",
                Map.of(
                        "orderId", orderId,
                        "conflictType", "SOFT_DELETED",
                        "suggestedAction", "REFRESH_ORDER_DATA"
                )
        );
    }
}
