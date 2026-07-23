package com.example.new_toy_store.supplier.domain;

import com.example.new_toy_store.supplier.domain.exception.InvalidSupplierOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SupplierStatus {

    ACTIVE("Đang hợp tác") {
        @Override
        public boolean canImport() { return true; }

        @Override
        public boolean canBeAssignedToProduct() { return true; }

        @Override
        public List<SupplierStatus> getNextValidStates() { return List.of(SUSPENDED, BLACKLISTED); }
    },

    SUSPENDED("Tạm ngưng") {
        @Override
        public boolean canImport() { return false; }

        @Override
        public boolean canBeAssignedToProduct() { return true; }

        @Override
        public List<SupplierStatus> getNextValidStates() { return List.of(ACTIVE, BLACKLISTED); }
    },

    BLACKLISTED("Cấm hợp tác") {
        @Override
        public boolean canImport() { return false; }

        @Override
        public boolean canBeAssignedToProduct() { return false; }

        @Override
        public List<SupplierStatus> getNextValidStates() { return List.of(ACTIVE); }
    };

    private final String displayName;

    SupplierStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getCode() { return name(); }
    public String getDisplayName() { return displayName; }
    public boolean isImportAllowed() { return canImport(); }
    public boolean isProductAssignmentAllowed() { return canBeAssignedToProduct(); }

    public abstract boolean canImport();
    public abstract boolean canBeAssignedToProduct();

    @JsonIgnore
    public abstract List<SupplierStatus> getNextValidStates();

    public boolean canTransitionTo(SupplierStatus targetStatus) {
        return targetStatus != null && getNextValidStates().contains(targetStatus);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SupplierStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidSupplierOperationException.emptyField("Trạng thái");
        }
        try {
            return SupplierStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidSupplierOperationException.invalidStatus(value);
        }
    }
}
