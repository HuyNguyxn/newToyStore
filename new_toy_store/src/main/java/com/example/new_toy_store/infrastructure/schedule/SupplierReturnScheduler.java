package com.example.new_toy_store.infrastructure.schedule;

import com.example.new_toy_store.supplier_return.application.facade.SupplierReturnFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SupplierReturnScheduler {

    private static final Logger log = LoggerFactory.getLogger(SupplierReturnScheduler.class);
    private final SupplierReturnFacade supplierReturnFacade;

    @Value("${app.supplier-return.sla.warning-hours}")
    private int warningHours;

    @Value("${app.supplier-return.sla.critical-hours}")
    private int criticalHours;

    public SupplierReturnScheduler(SupplierReturnFacade supplierReturnFacade) {
        this.supplierReturnFacade = supplierReturnFacade;
    }

    @Scheduled(
            cron = "${app.supplier-return.sla.cron}",
            zone = "${app.supplier-return.sla.zone}"
    )
    public void executeSlaCheck() {
        log.info("[SCHEDULE] Khởi chạy kiểm tra SLA Phiếu Trả Hàng (Warning: {}h, Critical: {}h)", warningHours, criticalHours);

        try {
            supplierReturnFacade.processSlaAlerts(warningHours, criticalHours);
            log.info("[SCHEDULE] Hoàn tất kiểm tra SLA Phiếu Trả Hàng.");
        } catch (Exception e) {
            log.error("[SCHEDULE] Lỗi khi chạy SLA Phiếu Trả Hàng: {}", e.getMessage(), e);
        }
    }
}
