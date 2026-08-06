package com.example.new_toy_store.customer_return.application.listener;

import com.example.new_toy_store.customer_return.application.service.CustomerReturnService;
import com.example.new_toy_store.global.event.ShipmentCancelledEvent;
import com.example.new_toy_store.global.event.ShipmentDeliveredEvent;
import com.example.new_toy_store.global.event.ShipmentInTransitEvent;
import com.example.new_toy_store.global.event.ShipmentReturnedEvent;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentResponse;
import com.example.new_toy_store.logistics.application.facade.LogisticsFacade;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerReturnLogisticsListener {

    private final CustomerReturnService customerReturnService;
    private final LogisticsFacade logisticsFacade;

    public CustomerReturnLogisticsListener(
            CustomerReturnService customerReturnService,
            LogisticsFacade logisticsFacade
    ) {
        this.customerReturnService = customerReturnService;
        this.logisticsFacade = logisticsFacade;
    }

    @EventListener
    public void onShipmentInTransit(ShipmentInTransitEvent event) {
        ShipmentResponse shipment = logisticsFacade.getShipmentDetails(event.shipmentId());
        if (shipment != null && shipment.getCustomerReturnId() != null) {
            customerReturnService.transitReturn(shipment.getCustomerReturnId(), "SYSTEM_CARRIER");
        }
    }

    @EventListener
    public void onShipmentDelivered(ShipmentDeliveredEvent event) {
        ShipmentResponse shipment = logisticsFacade.getShipmentDetails(event.shipmentId());
        if (shipment != null && shipment.getCustomerReturnId() != null) {
            customerReturnService.receiveItems(shipment.getCustomerReturnId(), "SYSTEM_CARRIER");
        }
    }

    @EventListener
    public void onShipmentReturned(ShipmentReturnedEvent event) {
        ShipmentResponse shipment = logisticsFacade.getShipmentDetails(event.shipmentId());
        if (shipment != null && shipment.getCustomerReturnId() != null) {
            customerReturnService.receiveItems(shipment.getCustomerReturnId(), "SYSTEM_WAREHOUSE");
        }
    }

    @EventListener
    public void onShipmentCancelled(ShipmentCancelledEvent event) {
        ShipmentResponse shipment = logisticsFacade.getShipmentDetails(event.shipmentId());
        if (shipment != null && shipment.getCustomerReturnId() != null) {
            customerReturnService.markShippingFailed(shipment.getCustomerReturnId(), event.reason());
        }
    }
}
