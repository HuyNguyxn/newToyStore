package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.order.application.dto.request.OrderFilterRequest;
import com.example.new_toy_store.order.domain.Order;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecification {

    private OrderSpecification() {}

    public static Specification<Order> filter(OrderFilterRequest request) {
        if (request == null) return Specification.where(null);

        return Specification.where(hasUserId(request.getUserId()))
                .and(hasStatus(request.getStatus()))
                .and(createdBetween(request))
                .and(amountBetween(request.getMinAmount(), request.getMaxAmount()));
    }

    public static Specification<Order> hasUserId(Integer userId) {
        return BaseSpecification.isEqual("userId", userId);
    }

    public static Specification<Order> hasStatus(Object status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<Order> createdBetween(OrderFilterRequest request) {
        if (request == null) return Specification.where(null);
        return BaseSpecification.dateBetween("createdAt", request.getFromDate(), request.getToDate());
    }

    public static Specification<Order> amountBetween(Double minAmount, Double maxAmount) {
        return (root, query, cb) -> {
            if (minAmount == null && maxAmount == null) return null;

            if (minAmount != null && maxAmount != null) {
                return cb.between(root.get("totalAmount"), minAmount, maxAmount);
            } else if (minAmount != null) {
                return cb.greaterThanOrEqualTo(root.get("totalAmount"), minAmount);
            } else {
                return cb.lessThanOrEqualTo(root.get("totalAmount"), maxAmount);
            }
        };
    }
}
