package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.supplier.application.dto.request.SupplierFilterRequest;
import com.example.new_toy_store.supplier.domain.Supplier;
import com.example.new_toy_store.supplier.domain.SupplierStatus;
import org.springframework.data.jpa.domain.Specification;

public final class SupplierSpecification {

    private SupplierSpecification() {}

    public static Specification<Supplier> filter(SupplierFilterRequest request) {
        if (request == null) return Specification.where(null);

        return Specification.where(hasName(request.getName()))
                .and(hasPhoneNumber(request.getPhoneNumber()))
                .and(hasStatus(request.getStatus()));
    }

    public static Specification<Supplier> hasName(String name) {
        return BaseSpecification.contains("name", name);
    }

    public static Specification<Supplier> hasPhoneNumber(String phoneNumber) {
        return BaseSpecification.contains("phoneNumber", phoneNumber);
    }

    public static Specification<Supplier> hasStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        return BaseSpecification.isEqual("status", SupplierStatus.from(status));
    }
}
