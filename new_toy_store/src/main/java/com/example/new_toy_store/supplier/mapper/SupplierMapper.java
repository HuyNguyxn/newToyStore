package com.example.new_toy_store.supplier.mapper;

import com.example.new_toy_store.supplier.application.dto.request.SupplierCreateRequest;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.domain.Supplier;
import com.example.new_toy_store.supplier.domain.SupplierStatus;

import java.util.List;

public final class SupplierMapper {

    private SupplierMapper() {}

    public static Supplier toNewSupplier(SupplierCreateRequest request) {
        return new Supplier(
                request.getName(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getAddress()
        );
    }

    public static Supplier toEntity(SupplierCreateRequest request) {
        return toNewSupplier(request);
    }

    public static SupplierResponse toResponse(Supplier supplier) {
        SupplierStatus status = supplier.getStatus();

        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getPhoneNumber(),
                supplier.getEmail(),
                supplier.getAddress(),
                status,
                toAvailableActions(status)
        );
    }

    private static List<SupplierStatus> toAvailableActions(SupplierStatus status) {
        if (status == null) {
            return List.of();
        }
        return status.getNextValidStates();
    }
}
