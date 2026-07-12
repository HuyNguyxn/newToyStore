package com.example.new_toy_store.supplier.domain;

import com.example.new_toy_store.supplier.domain.exception.InvalidSupplierOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SupplierStatus {

    ACTIVE("Đang hợp tác") {
        @Override
        public boolean canImport() { return true; }
        @Override
        public boolean canBeAssignedToProduct() { return true; }
        @Override
        public List<SupplierStatus> getNextValidStates() { return Arrays.asList(SUSPENDED, BLACKLISTED); }
    },

    SUSPENDED("Tạm ngưng") {
        @Override
        public boolean canImport() { return false; }
        @Override
        public boolean canBeAssignedToProduct() { return true; }
        @Override
        public List<SupplierStatus> getNextValidStates() { return Arrays.asList(ACTIVE, BLACKLISTED); }
    },

    BLACKLISTED("Cấm hợp tác") {
        @Override
        public boolean canImport() { return false; }
        @Override
        public boolean canBeAssignedToProduct() { return false; }
        @Override
        public List<SupplierStatus> getNextValidStates() { return Collections.singletonList(ACTIVE); }
    };

    private final String displayName;

    SupplierStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public abstract boolean canImport();
    public abstract boolean canBeAssignedToProduct();
    public abstract List<SupplierStatus> getNextValidStates();

    @JsonCreator
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