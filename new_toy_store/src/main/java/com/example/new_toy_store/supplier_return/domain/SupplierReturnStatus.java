package com.example.new_toy_store.supplier_return.domain;

import com.example.new_toy_store.supplier_return.domain.exception.InvalidSupplierReturnOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum SupplierReturnStatus {

    DRAFT("Bản nháp") {
        @Override
        public List<SupplierReturnStatus> getNextValidStates() {
            return Arrays.asList(PENDING_APPROVAL, CANCELLED);
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }
    },

    PENDING_APPROVAL("Chờ duyệt") {
        @Override
        public List<SupplierReturnStatus> getNextValidStates() {
            return Arrays.asList(APPROVED, REJECTED);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    APPROVED("Đã duyệt - Chờ xuất kho") {
        @Override
        public List<SupplierReturnStatus> getNextValidStates() {
            return Arrays.asList(SHIPPED, CANCELLED);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    SHIPPED("Đang xuất trả") {
        @Override
        public List<SupplierReturnStatus> getNextValidStates() {
            return Collections.singletonList(COMPLETED);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    COMPLETED("Hoàn thành") {
        @Override
        public List<SupplierReturnStatus> getNextValidStates() {
            return Collections.emptyList();
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    REJECTED("Từ chối duyệt") {
        @Override
        public List<SupplierReturnStatus> getNextValidStates() {
            return Collections.emptyList();
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    CANCELLED("Đã hủy") {
        @Override
        public List<SupplierReturnStatus> getNextValidStates() {
            return Collections.emptyList();
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    };

    private final String displayName;

    SupplierReturnStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract List<SupplierReturnStatus> getNextValidStates();

    public abstract boolean isReadOnly();

    public boolean canTransitionTo(SupplierReturnStatus nextState) {
        return getNextValidStates().contains(nextState);
    }

    public List<String> getNextValidStateNames() {
        return getNextValidStates().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @JsonCreator
    public static SupplierReturnStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidSupplierReturnOperationException.emptyField("Trạng thái");
        }
        try {
            return SupplierReturnStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidSupplierReturnOperationException.invalidStatus(value);
        }
    }
}