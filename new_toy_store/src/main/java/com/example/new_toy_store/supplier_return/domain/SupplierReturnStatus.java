package com.example.new_toy_store.supplier_return.domain;

import com.example.new_toy_store.supplier_return.domain.exception.InvalidSupplierReturnOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SupplierReturnStatus {

    DRAFT("DRAFT", "Bản nháp") {
        @Override
        protected List<SupplierReturnStatus> nextStates() {
            return Arrays.asList(PENDING_APPROVAL, CANCELLED);
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }
    },

    PENDING_APPROVAL("PENDING_APPROVAL", "Chờ duyệt") {
        @Override
        protected List<SupplierReturnStatus> nextStates() {
            return Arrays.asList(APPROVED, REJECTED);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    APPROVED("APPROVED", "Đã duyệt - Chờ xuất kho") {
        @Override
        protected List<SupplierReturnStatus> nextStates() {
            return Arrays.asList(SHIPPED, CANCELLED);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    SHIPPED("SHIPPED", "Đang xuất trả") {
        @Override
        protected List<SupplierReturnStatus> nextStates() {
            return List.of(COMPLETED);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    COMPLETED("COMPLETED", "Hoàn thành") {
        @Override
        protected List<SupplierReturnStatus> nextStates() {
            return Collections.emptyList();
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    REJECTED("REJECTED", "Từ chối duyệt") {
        @Override
        protected List<SupplierReturnStatus> nextStates() {
            return Collections.emptyList();
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    },

    CANCELLED("CANCELLED", "Đã hủy") {
        @Override
        protected List<SupplierReturnStatus> nextStates() {
            return Collections.emptyList();
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    };

    private final String code;
    private final String displayName;

    SupplierReturnStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    protected abstract List<SupplierReturnStatus> nextStates();

    public abstract boolean isReadOnly();

    @JsonIgnore
    public List<SupplierReturnStatus> getNextValidStates() {
        return nextStates();
    }

    public List<String> getAllowedNextStatusCodes() {
        return nextStates().stream()
                .map(SupplierReturnStatus::getCode)
                .toList();
    }

    public boolean canTransitionTo(SupplierReturnStatus nextState) {
        return nextState != null && nextStates().contains(nextState);
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SupplierReturnStatus from(Object input) {
        if (input == null) {
            throw InvalidSupplierReturnOperationException.emptyField("Trạng thái");
        }

        if (input instanceof Map<?, ?> objectValue) {
            Object codeValue = objectValue.get("code");
            if (codeValue == null) {
                codeValue = objectValue.get("name");
            }
            if (codeValue == null) {
                codeValue = objectValue.get("status");
            }
            return fromText(String.valueOf(codeValue));
        }

        return fromText(String.valueOf(input));
    }

    public static SupplierReturnStatus from(String value) {
        return fromText(value);
    }

    private static SupplierReturnStatus fromText(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            throw InvalidSupplierReturnOperationException.emptyField("Trạng thái");
        }
        String normalized = value.trim().toUpperCase();
        for (SupplierReturnStatus status : values()) {
            if (status.name().equals(normalized) || status.code.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw InvalidSupplierReturnOperationException.invalidStatus(value);
    }
}
