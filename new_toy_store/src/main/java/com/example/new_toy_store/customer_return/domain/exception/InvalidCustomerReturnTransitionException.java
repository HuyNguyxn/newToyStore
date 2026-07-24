package com.example.new_toy_store.customer_return.domain.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class InvalidCustomerReturnTransitionException extends RuntimeException {

    private final String currentState;
    private final String nextState;

    public InvalidCustomerReturnTransitionException(String currentState, String nextState) {
        super("Không thể chuyển trạng thái yêu cầu trả hàng từ '" + currentState + "' sang '" + nextState + "'.");
        this.currentState = currentState;
        this.nextState = nextState;
    }

    public String getErrorType() {
        return "CUSTOMER_RETURN_INVALID_STATE_TRANSITION";
    }

    public Map<String, Object> getContextData() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("currentState", currentState);
        context.put("attemptedState", nextState);
        context.put("businessRule", "CustomerReturnStatus.canTransitionTo");
        return context;
    }
}
