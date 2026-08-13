package com.example.new_toy_store.order.application;

import com.example.new_toy_store.global.event.ShipmentCancelledEvent;
import com.example.new_toy_store.global.event.ShipmentDeliveredEvent;
import com.example.new_toy_store.global.event.ShipmentInTransitEvent;
import com.example.new_toy_store.order.domain.exception.InvalidOrderOperationException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderShipmentEventListener {

    private final OrderService orderService;

    public OrderShipmentEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener
    public void handleShipmentInTransit(ShipmentInTransitEvent event) {
        if (event.orderId() == null) return;
        try {
            orderService.ship(event.orderId(), "Shipment " + event.trackingCode() + " is in transit");
        } catch (InvalidOrderOperationException ignored) {
        }
    }

    @EventListener
    public void handleShipmentDelivered(ShipmentDeliveredEvent event) {
        if (event.orderId() == null) return;
        try {
            orderService.complete(event.orderId(), "Shipment " + event.trackingCode() + " delivered");
        } catch (InvalidOrderOperationException ignored) {
        }
    }

    @EventListener
    public void handleShipmentCancelled(ShipmentCancelledEvent event) {
        if (event.orderId() == null) return;
        try {
            orderService.cancel(event.orderId(), event.reason());
        } catch (InvalidOrderOperationException ignored) {
        }
    }
}
