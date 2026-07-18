package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.customer_return.domain.CustomerReturn;
import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import com.example.new_toy_store.global.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

public class CustomerReturnSpecification {

    public static Specification<CustomerReturn> filter(String status, Integer orderId) {
        Specification<CustomerReturn> spec = Specification.where(BaseSpecification.isDistinct());

        if (status != null && !status.trim().isEmpty()) {
            spec = spec.and(BaseSpecification.isEqual("status", CustomerReturnStatus.from(status)));
        }

        return spec.and(BaseSpecification.isEqual("orderId", orderId));
    }
}