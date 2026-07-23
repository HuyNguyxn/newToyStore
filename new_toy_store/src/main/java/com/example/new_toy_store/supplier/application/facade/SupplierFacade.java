package com.example.new_toy_store.supplier.application.facade;

import com.example.new_toy_store.supplier.application.SupplierService;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.domain.exception.InvalidSupplierOperationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@Transactional(readOnly = true)
public class SupplierFacade {

    private final SupplierService supplierService;

    public SupplierFacade(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    public SupplierResponse getSupplierDetails(Integer supplierId) {
        if (supplierId == null) return null;

        return supplierService.getSupplierDetails(supplierId);
    }

    public SupplierResponse getRequiredSupplierDetails(Integer supplierId, String sourceModule) {
        if (supplierId == null) {
            throw InvalidSupplierOperationException.missingExternalReference("supplierId", sourceModule);
        }

        return supplierService.getSupplierDetails(supplierId);
    }

    public List<SupplierResponse> getSuppliersByIds(Set<Integer> supplierIds) {
        if (supplierIds == null || supplierIds.isEmpty()) return List.of();

        return supplierService.getSuppliersByIds(supplierIds);
    }
}
