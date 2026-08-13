package com.example.new_toy_store.customer_payment.application.listener;

import com.example.new_toy_store.customer_payment.application.service.PaymentService;
import com.example.new_toy_store.global.event.OrderCancelledEvent;
import com.example.new_toy_store.global.event.OrderStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CustomerPaymentOrderEventListener {

    private final PaymentService paymentService;

    public CustomerPaymentOrderEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        paymentService.recordCodCollected(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        paymentService.requestRefundForCancelledOrder(event.getOrderId(), event.getReason());
    }
}
