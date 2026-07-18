package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.order.application.dto.request.OrderFilterRequest;
import com.example.new_toy_store.order.domain.Order;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {

    public static Specification<Order> filter(OrderFilterRequest request) {
        if (request == null) return Specification.where(null);

        return Specification.where(BaseSpecification.<Order>isEqual("userId", request.getUserId()))
                .and(BaseSpecification.isEqual("status", request.getStatus()))
                .and(BaseSpecification.dateBetween("createdAt", request.getFromDate(), request.getToDate()))
                .and(amountBetween(request.getMinAmount(), request.getMaxAmount()));
    }

    private static Specification<Order> amountBetween(Double minAmount, Double maxAmount) {
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