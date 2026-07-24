package com.example.new_toy_store.imports.domain;

import com.example.new_toy_store.imports.domain.exception.InvalidImportOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ImportStatus {

    PENDING(
            "PENDING",
            "Chờ kiểm đếm",
            "Phiếu nhập mới tạo, còn được phép chỉnh sửa item."
    ) {
        @Override
        protected Set<ImportStatus> nextStatuses() {
            return EnumSet.of(COMPLETED, CANCELLED);
        }

        @Override
        public boolean canModifyItems() {
            return true;
        }
    },

    COMPLETED(
            "COMPLETED",
            "Đã nhập kho",
            "Phiếu nhập đã hoàn tất và hàng đã được cộng vào kho."
    ) {
        @Override
        protected Set<ImportStatus> nextStatuses() {
            return EnumSet.noneOf(ImportStatus.class);
        }
    },

    CANCELLED(
            "CANCELLED",
            "Đã hủy",
            "Phiếu nhập đã bị hủy và không còn được xử lý tiếp."
    ) {
        @Override
        protected Set<ImportStatus> nextStatuses() {
            return EnumSet.noneOf(ImportStatus.class);
        }
    };

    private final String code;
    private final String displayName;
    private final String description;

    ImportStatus(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    protected abstract Set<ImportStatus> nextStatuses();

    public boolean canModifyItems() {
        return false;
    }

    public boolean canTransitionTo(ImportStatus nextStatus) {
        return nextStatus != null && nextStatuses().contains(nextStatus);
    }

    public boolean canComplete() {
        return canTransitionTo(COMPLETED);
    }

    public boolean canCancel() {
        return canTransitionTo(CANCELLED);
    }

    public List<String> getAllowedNextStatuses() {
        return nextStatuses().stream()
                .map(ImportStatus::getCode)
                .toList();
    }

    public boolean isTerminal() {
        return nextStatuses().isEmpty();
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ImportStatus from(Object input) {
        if (input == null) {
            throw InvalidImportOperationException.emptyStatus();
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

    public static ImportStatus from(String value) {
        return fromText(value);
    }

    private static ImportStatus fromText(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            throw InvalidImportOperationException.emptyStatus();
        }

        String normalized = value.trim().toUpperCase();
        for (ImportStatus status : values()) {
            if (status.name().equals(normalized) || status.code.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }

        throw InvalidImportOperationException.invalidStatus(value);
    }
}
