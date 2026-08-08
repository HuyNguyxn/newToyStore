package com.example.new_toy_store.supplier_payment.domain;

import com.example.new_toy_store.supplier_payment.domain.exception.InvalidSupplierPaymentOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SupplierPaymentStatus {
    PENDING("Chưa thanh toán", "Khoản phải trả đã được tạo nhưng chưa chi tiền") {
        @Override
        public List<SupplierPaymentStatus> getNextValidStates() {
            return List.of(PARTIALLY_PAID, PAID, CANCELLED, OVERDUE);
        }
    },
    PARTIALLY_PAID("Thanh toán một phần", "Đã chi một phần tiền cho nhà cung cấp") {
        @Override
        public List<SupplierPaymentStatus> getNextValidStates() {
            return List.of(PAID, OVERDUE, CANCELLED);
        }
    },
    PAID("Đã thanh toán", "Đã chi đủ tiền cho nhà cung cấp") {
        @Override
        public List<SupplierPaymentStatus> getNextValidStates() {
            return List.of();
        }
    },
    OVERDUE("Quá hạn", "Khoản phải trả đã quá hạn thanh toán") {
        @Override
        public List<SupplierPaymentStatus> getNextValidStates() {
            return List.of(PARTIALLY_PAID, PAID, CANCELLED);
        }
    },
    CANCELLED("Đã hủy", "Khoản phải trả không còn cần thanh toán") {
        @Override
        public List<SupplierPaymentStatus> getNextValidStates() {
            return List.of();
        }
    };

    private final String displayName;
    private final String description;

    SupplierPaymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    @JsonProperty("code")
    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public abstract List<SupplierPaymentStatus> getNextValidStates();

    public boolean canTransitionTo(SupplierPaymentStatus targetStatus) {
        return getNextValidStates().contains(targetStatus);
    }

    public boolean isClosed() {
        return this == PAID || this == CANCELLED;
    }

    @JsonCreator
    public static SupplierPaymentStatus from(Object rawValue) {
        if (rawValue == null) {
            throw InvalidSupplierPaymentOperationException.emptyField("Trạng thái thanh toán nhà cung cấp");
        }

        String value = rawValue instanceof String stringValue
                ? stringValue
                : String.valueOf(rawValue);

        if (value.isBlank()) {
            throw InvalidSupplierPaymentOperationException.emptyField("Trạng thái thanh toán nhà cung cấp");
        }

        try {
            return SupplierPaymentStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidSupplierPaymentOperationException.invalidStatus(value);
        }
    }
}
