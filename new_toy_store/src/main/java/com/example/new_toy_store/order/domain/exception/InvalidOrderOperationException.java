package com.example.new_toy_store.order.domain.exception;

public class InvalidOrderOperationException extends RuntimeException {
    private final String currentStatus;
    private final String attemptedAction;

    public InvalidOrderOperationException(String currentStatus, String attemptedAction) {
        super(String.format("Không thể thực hiện thao tác '%s' khi đơn hàng đang ở trạng thái: %s", attemptedAction, currentStatus));
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public String getCurrentStatus() { return currentStatus; }
    public String getAttemptedAction() { return attemptedAction; }
}