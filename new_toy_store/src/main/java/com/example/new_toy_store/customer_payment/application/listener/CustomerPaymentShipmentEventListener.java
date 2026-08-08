package com.example.new_toy_store.customer_payment.application.listener;

import com.example.new_toy_store.global.event.ShipmentDeliveredEvent;
import com.example.new_toy_store.customer_payment.application.service.PaymentService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerPaymentShipmentEventListener {

    private final PaymentService paymentService;

    public CustomerPaymentShipmentEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @EventListener
    public void handleShipmentDelivered(ShipmentDeliveredEvent event) {
        paymentService.recordCodCollected(event);
    }
}
