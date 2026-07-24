package com.example.new_toy_store.order.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class OrderCrossModuleException extends OrderDomainException {

    private OrderCrossModuleException(String message, String sourceModule, Object invalidValue) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "ORDER_CROSS_MODULE_INVALID_DATA",
                message,
                Map.of(
                        "sourceModule", sourceModule,
                        "invalidValue", invalidValue == null ? "" : invalidValue,
                        "reason", "INVALID_CROSS_MODULE_REFERENCE"
                )
        );
    }

    public static OrderCrossModuleException missingCustomer(Integer userId) {
        return new OrderCrossModuleException(
                "Module User không tìm thấy khách hàng có ID " + userId + " để tạo đơn hàng.",
                "User",
                userId
        );
    }

    public static OrderCrossModuleException missingProduct(Integer productId) {
        return new OrderCrossModuleException(
                "Module Product không tìm thấy sản phẩm có ID " + productId + " để tạo đơn hàng.",
                "Product",
                productId
        );
    }

    public static OrderCrossModuleException missingVariant(Integer variantId) {
        return new OrderCrossModuleException(
                "Module Product không tìm thấy biến thể sản phẩm có ID " + variantId + " để tạo đơn hàng.",
                "Product",
                variantId
        );
    }
}
