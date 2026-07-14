package com.example.new_toy_store.customer_return.domain.exception;

import java.util.Map;

public class InvalidCustomerReturnTransitionException extends RuntimeException {
    private final String currentState;
    private final String nextState;

    public InvalidCustomerReturnTransitionException(String currentState, String nextState) {
        super("Không thể chuyển trạng thái từ [" + currentState + "] sang [" + nextState + "].");
        this.currentState = currentState;
        this.nextState = nextState;
    }

    public Map<String, Object> getContext() {
        return Map.of("currentState", currentState, "attemptedState", nextState);
    }
}