package com.example.new_toy_store.supplier.infrastructure.specification;

import com.example.new_toy_store.supplier.application.dto.request.SupplierFilterRequest;
import com.example.new_toy_store.supplier.domain.Supplier;
import com.example.new_toy_store.supplier.domain.SupplierStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SupplierSpecification {
    public static Specification<Supplier> filter(SupplierFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
            }
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("phoneNumber"), "%" + request.getPhoneNumber() + "%"));
            }
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), SupplierStatus.from(request.getStatus())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}