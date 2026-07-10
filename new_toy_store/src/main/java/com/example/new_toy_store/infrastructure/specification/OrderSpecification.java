package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.order.application.dto.request.OrderFilterRequest;
import com.example.new_toy_store.order.domain.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> filter(OrderFilterRequest filterParams) {
        return (root, query, cb) -> {

            List<Predicate> filterConditions = new ArrayList<>();

            if (filterParams.getUserId() != null) {
                Predicate userIdCondition = cb.equal(root.get("userId"), filterParams.getUserId());
                filterConditions.add(userIdCondition);
            }

            if (filterParams.getStatus() != null) {
                Predicate statusCondition = cb.equal(root.get("status"), filterParams.getStatus());
                filterConditions.add(statusCondition);
            }

            if (filterParams.getFromDate() != null) {
                Predicate fromDateCondition = cb.greaterThanOrEqualTo(root.get("createdAt"), filterParams.getFromDate());
                filterConditions.add(fromDateCondition);
            }

            if (filterParams.getToDate() != null) {
                Predicate toDateCondition = cb.lessThanOrEqualTo(root.get("createdAt"), filterParams.getToDate());
                filterConditions.add(toDateCondition);
            }

            if (filterParams.getMinAmount() != null) {
                Predicate minAmountCondition = cb.greaterThanOrEqualTo(root.get("totalAmount"), filterParams.getMinAmount());
                filterConditions.add(minAmountCondition);
            }

            if (filterParams.getMaxAmount() != null) {
                Predicate maxAmountCondition = cb.lessThanOrEqualTo(root.get("totalAmount"), filterParams.getMaxAmount());
                filterConditions.add(maxAmountCondition);
            }

            return cb.and(filterConditions.toArray(new Predicate[0]));
        };
    }
}