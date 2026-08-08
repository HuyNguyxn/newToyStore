package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.customer_return.domain.CustomerReturn;
import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.order.domain.Order;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerReturnSpecification {

    private CustomerReturnSpecification() {
    }

    public static Specification<CustomerReturn> filter(String status, Integer orderId) {
        return Specification.where(BaseSpecification.<CustomerReturn>isDistinct())
                .and(hasStatus(status))
                .and(hasOrderId(orderId));
    }

    public static Specification<CustomerReturn> filterForCustomer(String status, Integer orderId, Integer customerId) {
        return filter(status, orderId).and(belongsToCustomer(customerId));
    }

    public static Specification<CustomerReturn> hasStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return Specification.where(null);
        }
        return hasStatus(CustomerReturnStatus.from(status));
    }

    public static Specification<CustomerReturn> hasStatus(CustomerReturnStatus status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<CustomerReturn> hasOrderId(Integer orderId) {
        return BaseSpecification.isEqual("orderId", orderId);
    }

    public static Specification<CustomerReturn> belongsToCustomer(Integer customerId) {
        if (customerId == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }
        return (root, query, criteriaBuilder) -> {
            Subquery<Integer> orderSubquery = query.subquery(Integer.class);
            Root<Order> orderRoot = orderSubquery.from(Order.class);
            orderSubquery.select(orderRoot.get("id"))
                    .where(criteriaBuilder.equal(orderRoot.get("userId"), customerId));
            return root.get("orderId").in(orderSubquery);
        };
    }
}
