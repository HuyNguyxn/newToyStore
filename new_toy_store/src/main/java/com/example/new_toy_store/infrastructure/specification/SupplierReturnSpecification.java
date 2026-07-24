package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.supplier_return.domain.SupplierReturn;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class SupplierReturnSpecification {

    private SupplierReturnSpecification() {
    }

    public static Specification<SupplierReturn> filter(Integer supplierId,
                                                       String status,
                                                       LocalDate startDate,
                                                       LocalDate endDate) {
        return Specification.where(hasSupplierId(supplierId))
                .and(hasStatus(status))
                .and(createdBetween(startDate, endDate));
    }

    public static Specification<SupplierReturn> hasSupplierId(Integer supplierId) {
        return BaseSpecification.isEqual("supplierId", supplierId);
    }

    public static Specification<SupplierReturn> hasStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return Specification.where(null);
        }
        return hasStatus(SupplierReturnStatus.from(status));
    }

    public static Specification<SupplierReturn> hasStatus(SupplierReturnStatus status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<SupplierReturn> createdBetween(LocalDate startDate, LocalDate endDate) {
        return BaseSpecification.dateBetween("createdAt", startDate, endDate);
    }
}
