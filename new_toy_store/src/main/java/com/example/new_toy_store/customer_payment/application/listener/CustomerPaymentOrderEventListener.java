package com.example.new_toy_store.customer_payment.application.listener;

import com.example.new_toy_store.customer_payment.application.service.PaymentService;
import com.example.new_toy_store.global.event.OrderStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
}
