package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.logistics.application.dto.request.ShipmentFilterRequest;
import com.example.new_toy_store.logistics.domain.Shipment;
import org.springframework.data.jpa.domain.Specification;

public final class ShipmentSpecification {

    private ShipmentSpecification() {}

    public static Specification<Shipment> filter(ShipmentFilterRequest request) {
        if (request == null) return Specification.where(null);

        return Specification.where(hasOrderId(request.getOrderId()))
                .and(hasUserId(request.getUserId()))
                .and(hasStatus(request.getStatus()))
                .and(hasProviderCode(request.getProviderCode()))
                .and(containsTrackingCode(request.getTrackingCode()))
                .and(createdBetween(request));
    }

    public static Specification<Shipment> hasOrderId(Integer orderId) {
        return BaseSpecification.isEqual("orderId", orderId);
    }

    public static Specification<Shipment> hasUserId(Integer userId) {
        return BaseSpecification.isEqual("userId", userId);
    }

    public static Specification<Shipment> hasStatus(Object status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<Shipment> hasProviderCode(Object providerCode) {
        return BaseSpecification.isEqual("providerCode", providerCode);
    }

    public static Specification<Shipment> containsTrackingCode(String trackingCode) {
        return BaseSpecification.contains("trackingCode", trackingCode);
    }

    public static Specification<Shipment> createdBetween(ShipmentFilterRequest request) {
        return BaseSpecification.dateBetween("createdAt", request.getFromDate(), request.getToDate());
    }
}
