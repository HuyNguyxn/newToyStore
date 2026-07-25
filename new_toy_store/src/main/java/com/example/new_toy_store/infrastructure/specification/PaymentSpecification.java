package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.payment.application.dto.request.PaymentFilterRequest;
import com.example.new_toy_store.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.domain.Specification;

public final class PaymentSpecification {

    private PaymentSpecification() {}

    public static Specification<PaymentTransaction> filter(PaymentFilterRequest request) {
        if (request == null) return Specification.where(null);

        return Specification.where(hasOrderId(request.getOrderId()))
                .and(hasUserId(request.getUserId()))
                .and(hasMethod(request.getMethod()))
                .and(hasStatus(request.getStatus()))
                .and(amountBetween(request.getMinAmount(), request.getMaxAmount()))
                .and(createdBetween(request));
    }

    public static Specification<PaymentTransaction> hasOrderId(Integer orderId) {
        return BaseSpecification.isEqual("orderId", orderId);
    }

    public static Specification<PaymentTransaction> hasUserId(Integer userId) {
        return BaseSpecification.isEqual("userId", userId);
    }

    public static Specification<PaymentTransaction> hasMethod(Object method) {
        return BaseSpecification.isEqual("method", method);
    }

    public static Specification<PaymentTransaction> hasStatus(Object status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<PaymentTransaction> createdBetween(PaymentFilterRequest request) {
        return BaseSpecification.dateBetween("createdAt", request.getFromDate(), request.getToDate());
    }

    public static Specification<PaymentTransaction> amountBetween(Double minAmount, Double maxAmount) {
        return (root, query, cb) -> {
            if (minAmount == null && maxAmount == null) return cb.conjunction();
            if (minAmount != null && maxAmount != null) {
                return cb.between(root.get("amount"), minAmount, maxAmount);
            }
            if (minAmount != null) {
                return cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
            }
            return cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
        };
    }
}
