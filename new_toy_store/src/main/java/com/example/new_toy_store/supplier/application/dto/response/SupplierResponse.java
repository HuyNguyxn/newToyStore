package com.example.new_toy_store.supplier.application.dto.response;

import com.example.new_toy_store.supplier.domain.SupplierStatus;

import java.util.List;

public class SupplierResponse {
    private final Integer id;
    private final String name;
    private final String phoneNumber;
    private final String email;
    private final String address;
    private final SupplierStatus status;
    private final List<SupplierStatus> availableActions;

    public SupplierResponse(
            Integer id,
            String name,
            String phoneNumber,
            String email,
            String address,
            SupplierStatus status,
            List<SupplierStatus> availableActions
    ) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.status = status;
        this.availableActions = availableActions;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public SupplierStatus getStatus() { return status; }
    public String getStatusDisplayName() { return status != null ? status.getDisplayName() : null; }
    public List<SupplierStatus> getAvailableActions() { return availableActions; }
}
