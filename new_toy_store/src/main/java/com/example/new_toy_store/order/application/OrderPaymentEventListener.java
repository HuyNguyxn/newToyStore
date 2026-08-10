package com.example.new_toy_store.order.application;

import com.example.new_toy_store.global.event.PaymentCompletedEvent;
import com.example.new_toy_store.order.domain.exception.InvalidOrderOperationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderPaymentEventListener {

    private final OrderService orderService;

    public OrderPaymentEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        if (!"PENDING".equals(orderService.getOrderStatus(event.orderId()))) {
            return;
        }

        try {
            orderService.confirm(event.orderId(), "Order confirmed after payment #" + event.paymentId());
        } catch (InvalidOrderOperationException ignored) {
            // Order may already be confirmed by staff; payment event stays idempotent for this simple project phase.
        }
    }
}
