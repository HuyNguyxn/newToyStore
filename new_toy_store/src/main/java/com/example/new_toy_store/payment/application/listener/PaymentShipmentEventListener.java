package com.example.new_toy_store.payment.application.listener;

import com.example.new_toy_store.global.event.ShipmentDeliveredEvent;
import com.example.new_toy_store.payment.application.service.PaymentService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentShipmentEventListener {

    private final PaymentService paymentService;

    public PaymentShipmentEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @EventListener
    public void handleShipmentDelivered(ShipmentDeliveredEvent event) {
        paymentService.recordCodCollected(event);
    }
}
