package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.customer_return.domain.CustomerReturn;
import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import com.example.new_toy_store.global.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

public class CustomerReturnSpecification {

    public static Specification<CustomerReturn> filter(String status, Integer orderId) {
        return Specification.where(BaseSpecification.<CustomerReturn>isDistinct())
                .and(hasStatus(status))
                .and(hasOrderId(orderId));
    }

    private static Specification<CustomerReturn> hasStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        return BaseSpecification.isEqual("status", CustomerReturnStatus.from(status));
    }

    private static Specification<CustomerReturn> hasOrderId(Integer orderId) {
        return BaseSpecification.isEqual("orderId", orderId);
    }
}