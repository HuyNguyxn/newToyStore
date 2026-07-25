package com.example.new_toy_store.logistics.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class LogisticsCrossModuleException extends LogisticsDomainException {

    private LogisticsCrossModuleException(String message, Map<String, Object> contextData) {
        super(HttpStatus.CONFLICT, "LOGISTICS_CROSS_MODULE_ERROR", message, contextData);
    }

    public static LogisticsCrossModuleException invalidOrder(Integer orderId, String reason) {
        return new LogisticsCrossModuleException(
                "Cannot create shipment for this order: " + reason,
                Map.of("orderId", orderId, "reason", reason)
        );
    }
}
