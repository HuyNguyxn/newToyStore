package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentFilterRequest;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentTransaction;
import org.springframework.data.jpa.domain.Specification;

public final class PaymentSpecification {

    private PaymentSpecification() {}

    public static Specification<CustomerPaymentTransaction> filter(CustomerPaymentFilterRequest request) {
        if (request == null) return Specification.where(null);

        return Specification.where(hasOrderId(request.getOrderId()))
                .and(hasUserId(request.getUserId()))
                .and(hasMethod(request.getMethod()))
                .and(hasStatus(request.getStatus()))
                .and(amountBetween(request.getMinAmount(), request.getMaxAmount()))
                .and(createdBetween(request));
    }

    public static Specification<CustomerPaymentTransaction> hasOrderId(Integer orderId) {
        return BaseSpecification.isEqual("orderId", orderId);
    }

    public static Specification<CustomerPaymentTransaction> hasUserId(Integer userId) {
        return BaseSpecification.isEqual("userId", userId);
    }

    public static Specification<CustomerPaymentTransaction> hasMethod(Object method) {
        return BaseSpecification.isEqual("method", method);
    }

    public static Specification<CustomerPaymentTransaction> hasStatus(Object status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<CustomerPaymentTransaction> createdBetween(CustomerPaymentFilterRequest request) {
        return BaseSpecification.dateBetween("createdAt", request.getFromDate(), request.getToDate());
    }

    public static Specification<CustomerPaymentTransaction> amountBetween(Double minAmount, Double maxAmount) {
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
