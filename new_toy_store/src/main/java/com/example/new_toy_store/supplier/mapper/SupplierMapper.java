package com.example.new_toy_store.supplier.mapper;

import com.example.new_toy_store.supplier.application.dto.request.SupplierCreateRequest;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.domain.Supplier;

import java.util.List;
import java.util.stream.Collectors;

public class SupplierMapper {

    public static Supplier toEntity(SupplierCreateRequest request) {
        return new Supplier(
                request.getName(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getAddress()
        );
    }

    public static SupplierResponse toResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getPhoneNumber(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getStatus().name(),
                supplier.getStatus().getDisplayName()
        );

        if (supplier.getStatus() != null) {
            List<String> actions = supplier.getStatus().getNextValidStates().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            response.setAvailableActions(actions);
        }

        return response;
    }
}
