package com.example.new_toy_store.supplier_return.application.listener;

import com.example.new_toy_store.global.event.ShipmentCancelledEvent;
import com.example.new_toy_store.global.event.ShipmentDeliveredEvent;
import com.example.new_toy_store.global.event.ShipmentInTransitEvent;
import com.example.new_toy_store.global.event.ShipmentReturnedEvent;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentResponse;
import com.example.new_toy_store.logistics.application.facade.LogisticsFacade;
import com.example.new_toy_store.supplier_return.application.SupplierReturnService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SupplierReturnLogisticsListener {

    private final SupplierReturnService supplierReturnService;
    private final LogisticsFacade logisticsFacade;

    public SupplierReturnLogisticsListener(
            SupplierReturnService supplierReturnService,
            LogisticsFacade logisticsFacade
    ) {
        this.supplierReturnService = supplierReturnService;
        this.logisticsFacade = logisticsFacade;
    }

    @EventListener
    public void onShipmentInTransit(ShipmentInTransitEvent event) {
        ShipmentResponse shipment = logisticsFacade.getShipmentDetails(event.shipmentId());
        if (shipment != null && shipment.getSupplierReturnId() != null) {
            supplierReturnService.shipAndDeductStock(shipment.getSupplierReturnId(), "SYSTEM_CARRIER");
        }
    }

    @EventListener
    public void onShipmentDelivered(ShipmentDeliveredEvent event) {
        ShipmentResponse shipment = logisticsFacade.getShipmentDetails(event.shipmentId());
        if (shipment != null && shipment.getSupplierReturnId() != null) {
            supplierReturnService.complete(shipment.getSupplierReturnId(), "SYSTEM_CARRIER");
        }
    }

    @EventListener
    public void onShipmentReturned(ShipmentReturnedEvent event) {
        ShipmentResponse shipment = logisticsFacade.getShipmentDetails(event.shipmentId());
        if (shipment != null && shipment.getSupplierReturnId() != null) {
            supplierReturnService.markShippingFailed(
                    shipment.getSupplierReturnId(),
                    "Hàng trả nhà cung cấp đã quay lại kho"
            );
        }
    }

    @EventListener
    public void onShipmentCancelled(ShipmentCancelledEvent event) {
        ShipmentResponse shipment = logisticsFacade.getShipmentDetails(event.shipmentId());
        if (shipment != null && shipment.getSupplierReturnId() != null) {
            supplierReturnService.markShippingFailed(shipment.getSupplierReturnId(), event.reason());
        }
    }
}
