package com.example.new_toy_store.order.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateActiveOrderException extends OrderDomainException {

    private DuplicateActiveOrderException(String message, Integer userId, String conflictKey) {
        super(
                HttpStatus.CONFLICT,
                "ORDER_ACTIVE_DUPLICATE",
                message,
                Map.of(
                        "userId", userId,
                        "conflictKey", conflictKey,
                        "conflictType", "ACTIVE"
                )
        );
    }

    public static DuplicateActiveOrderException promoAlreadyUsed(Integer userId, String promoCode) {
        return new DuplicateActiveOrderException(
                "Người dùng đã sử dụng mã khuyến mãi '" + promoCode + "' cho một đơn hàng đang hoạt động.",
                userId,
                promoCode
        );
    }
}
