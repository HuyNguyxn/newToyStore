package com.example.new_toy_store.infrastructure.schedule;

import com.example.new_toy_store.cart.application.service.CartMaintenanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CartMaintenance {

    private final CartMaintenanceService maintenanceService;

    public CartMaintenance(CartMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @Scheduled(cron = "${app.cart.cleanup.cron}", zone = "${app.cart.cleanup.zone}")
    public void execute() {
        maintenanceService.executeLifecycle();
    }
}