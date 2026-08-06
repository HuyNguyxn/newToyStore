package com.example.new_toy_store.logistics.application.listener;

import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import com.example.new_toy_store.global.event.CustomerReturnStatusChangedEvent;
import com.example.new_toy_store.global.event.SupplierReturnStatusChangedEvent;
import com.example.new_toy_store.logistics.application.LogisticsService;
import com.example.new_toy_store.order.application.dto.response.OrderLogisticsSnapshot;
import com.example.new_toy_store.order.application.facade.OrderFacade;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.application.facade.SupplierFacade;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LogisticsEventListener {

    private final LogisticsService logisticsService;
    private final OrderFacade orderFacade;
    private final SupplierFacade supplierFacade;

    public LogisticsEventListener(
            LogisticsService logisticsService,
            OrderFacade orderFacade,
            SupplierFacade supplierFacade
    ) {
        this.logisticsService = logisticsService;
        this.orderFacade = orderFacade;
        this.supplierFacade = supplierFacade;
    }

    @EventListener
    public void onCustomerReturnStatusChanged(CustomerReturnStatusChangedEvent event) {
        var payload = event.payload();
        if (payload.currentStatus() == CustomerReturnStatus.APPROVED) {
            OrderLogisticsSnapshot order = orderFacade.getLogisticsSnapshot(payload.orderId());
            logisticsService.createForCustomerReturn(
                    payload.returnId(),
                    payload.userId(),
                    "Customer #" + order.getUserId(),
                    null,
                    order.getShippingAddress(),
                    0.0
            );
        }
    }

    @EventListener
    public void onSupplierReturnStatusChanged(SupplierReturnStatusChangedEvent event) {
        var payload = event.payload();
        if (payload.currentStatus() == SupplierReturnStatus.APPROVED) {
            SupplierResponse supplier = supplierFacade.getSupplierDetails(payload.supplierId());
            logisticsService.createForSupplierReturn(
                    payload.returnId(),
                    1, // Default Admin/System User ID
                    supplier.getName(),
                    supplier.getPhoneNumber(),
                    supplier.getAddress(),
                    0.0
            );
        }
    }
}
