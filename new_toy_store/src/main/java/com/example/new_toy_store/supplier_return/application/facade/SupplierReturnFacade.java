package com.example.new_toy_store.supplier_return.application.facade;

import com.example.new_toy_store.supplier_return.application.SupplierReturnService;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnResponse;
import com.example.new_toy_store.supplier_return.domain.SupplierReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SupplierReturnFacade {

    private final SupplierReturnService supplierReturnService;

    public SupplierReturnFacade(SupplierReturnService supplierReturnService) {
        this.supplierReturnService = supplierReturnService;
    }

    public Page<SupplierReturnResponse> filterReturns(Integer supplierId,
                                                      String status,
                                                      LocalDate startDate,
                                                      LocalDate endDate,
                                                      Pageable pageable) {
        return supplierReturnService.filterReturns(supplierId, status, startDate, endDate, pageable);
    }

    public SupplierReturnResponse getDetail(Integer id) {
        return supplierReturnService.getDetail(id);
    }

    public List<SupplierReturn> getReturnsForCriticalAlert(int criticalHours) {
        return supplierReturnService.getReturnsForCriticalAlert(criticalHours);
    }

    public void processSlaAlerts(int warningHours, int criticalHours) {
        supplierReturnService.processSlaAlerts(warningHours, criticalHours);
    }
}
