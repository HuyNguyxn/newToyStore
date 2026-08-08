package com.example.new_toy_store.logistics.application.listener;

import com.example.new_toy_store.global.event.OrderStatusChangedEvent;
import com.example.new_toy_store.logistics.application.LogisticsService;
import com.example.new_toy_store.order.domain.OrderStatus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LogisticsOrderEventListener {

    private final LogisticsService logisticsService;

    public LogisticsOrderEventListener(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.currentStatus() == OrderStatus.CONFIRMED) {
            logisticsService.createForConfirmedOrder(event.orderId());
        } else if (event.currentStatus() == OrderStatus.SHIPPED || event.currentStatus() == OrderStatus.COMPLETED || event.currentStatus() == OrderStatus.CANCELLED) {
            logisticsService.syncShipmentStatusWithOrder(event.orderId(), event.currentStatus(), event.note());
        }
    }
}
