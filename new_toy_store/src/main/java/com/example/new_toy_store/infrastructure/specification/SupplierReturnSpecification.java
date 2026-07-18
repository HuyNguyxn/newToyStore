package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.supplier_return.domain.SupplierReturn;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class SupplierReturnSpecification {

    public static Specification<SupplierReturn> filter(
            Integer supplierId,
            String statusValue,
            LocalDate startDate,
            LocalDate endDate) {

        Specification<SupplierReturn> spec = Specification.where(BaseSpecification.<SupplierReturn>isEqual("supplierId", supplierId));

        if (statusValue != null && !statusValue.trim().isEmpty()) {
            spec = spec.and(BaseSpecification.isEqual("status", SupplierReturnStatus.from(statusValue)));
        }

        return spec.and(BaseSpecification.dateBetween("createdAt", startDate, endDate));
    }
}