package com.example.new_toy_store.order.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidOrderOperationException extends OrderDomainException {

    private final String currentStatus;
    private final String attemptedAction;

    public InvalidOrderOperationException(String currentStatus, String attemptedAction) {
        super(
                HttpStatus.CONFLICT,
                "ORDER_INVALID_OPERATION",
                "Không thể thực hiện thao tác '" + attemptedAction + "' khi đơn hàng đang ở trạng thái " + currentStatus + ".",
                Map.of(
                        "currentStatus", currentStatus,
                        "attemptedAction", attemptedAction,
                        "reason", "BUSINESS_RULE_VIOLATION"
                )
        );
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public String getCurrentStatus() { return currentStatus; }
    public String getAttemptedAction() { return attemptedAction; }
}
