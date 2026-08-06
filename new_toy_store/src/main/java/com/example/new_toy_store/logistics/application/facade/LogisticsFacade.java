package com.example.new_toy_store.logistics.application.facade;

import com.example.new_toy_store.logistics.application.LogisticsService;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentResponse;
import org.springframework.stereotype.Component;

@Component
public class LogisticsFacade {

    private final LogisticsService logisticsService;

    public LogisticsFacade(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    public ShipmentResponse createForConfirmedOrder(Integer orderId) {
        return logisticsService.createForConfirmedOrder(orderId);
    }

    public ShipmentResponse getShipmentDetails(Integer shipmentId) {
        return logisticsService.getDetails(shipmentId, 1, true);
    }
}
