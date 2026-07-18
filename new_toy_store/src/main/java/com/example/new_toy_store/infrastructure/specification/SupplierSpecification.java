package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.supplier.application.dto.request.SupplierFilterRequest;
import com.example.new_toy_store.supplier.domain.Supplier;
import com.example.new_toy_store.supplier.domain.SupplierStatus;
import org.springframework.data.jpa.domain.Specification;

public class SupplierSpecification {

    public static Specification<Supplier> filter(SupplierFilterRequest request) {
        if (request == null) return Specification.where(null);

        Specification<Supplier> spec = Specification.where(BaseSpecification.<Supplier>contains("name", request.getName()))
                .and(BaseSpecification.contains("phoneNumber", request.getPhoneNumber()));

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            spec = spec.and(BaseSpecification.isEqual("status", SupplierStatus.from(request.getStatus())));
        }

        return spec;
    }
}