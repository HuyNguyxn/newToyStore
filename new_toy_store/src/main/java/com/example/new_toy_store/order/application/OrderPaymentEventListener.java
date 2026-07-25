package com.example.new_toy_store.order.application;

import com.example.new_toy_store.global.event.PaymentCompletedEvent;
import com.example.new_toy_store.order.domain.exception.InvalidOrderOperationException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentEventListener {

    private final OrderService orderService;

    public OrderPaymentEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            orderService.confirm(event.orderId(), "Order confirmed after payment #" + event.paymentId());
        } catch (InvalidOrderOperationException ignored) {
            // Order may already be confirmed by staff; payment event stays idempotent for this simple project phase.
        }
    }
}
