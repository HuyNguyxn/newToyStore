package com.example.new_toy_store.customer_return.domain;

import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CustomerReturnStatus {
    REQUESTED("REQUESTED", "Yêu cầu mới") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Arrays.asList(APPROVED, REJECTED, NEEDS_MORE_INFO, CANCELLED);
        }
    },
    NEEDS_MORE_INFO("NEEDS_MORE_INFO", "Cần bổ sung thông tin") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Arrays.asList(REQUESTED, REJECTED, CANCELLED);
        }
    },
    APPROVED("APPROVED", "Đã duyệt - Chờ bưu tá lấy hàng") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Arrays.asList(RETURNING, CANCELLED);
        }
    },
    RETURNING("RETURNING", "Đang hoàn về kho") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Collections.singletonList(RECEIVED);
        }
    },
    RECEIVED("RECEIVED", "Kho đã nhận - Đang kiểm định QC") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Arrays.asList(INSPECTED_OK, INSPECTED_FAILED);
        }
    },
    INSPECTED_OK("INSPECTED_OK", "Kiểm định đạt - Chờ xử lý hoàn tiền/đổi trả") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Arrays.asList(REFUNDED, REPLACED);
        }
    },
    INSPECTED_FAILED("INSPECTED_FAILED", "Kiểm định thất bại - Hàng không đúng mô tả") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Arrays.asList(REJECTED, DISPUTED);
        }
    },
    REJECTED("REJECTED", "Từ chối trả hàng") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return List.of(DISPUTED);
        }
    },
    DISPUTED("DISPUTED", "Đang tranh chấp / Khiếu nại") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Arrays.asList(REFUNDED, REPLACED, REJECTED);
        }
    },
    CANCELLED("CANCELLED", "Đã hủy") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Collections.emptyList();
        }
    },
    REFUNDED("REFUNDED", "Đã hoàn tiền") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Collections.emptyList();
        }
    },
    REPLACED("REPLACED", "Đã gửi hàng thay thế") {
        @Override
        protected List<CustomerReturnStatus> nextStates() {
            return Collections.emptyList();
        }
    };

    private final String code;
    private final String displayName;

    CustomerReturnStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    protected abstract List<CustomerReturnStatus> nextStates();

    @JsonIgnore
    public List<CustomerReturnStatus> getNextValidStates() {
        return nextStates();
    }

    public List<String> getAllowedNextStatusCodes() {
        return nextStates().stream()
                .map(CustomerReturnStatus::getCode)
                .toList();
    }

    public boolean canTransitionTo(CustomerReturnStatus nextState) {
        return nextState != null && nextStates().contains(nextState);
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CustomerReturnStatus from(Object input) {
        if (input == null) {
            throw InvalidCustomerReturnDataException.emptyField("Trạng thái trả hàng");
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

    public static CustomerReturnStatus from(String value) {
        return fromText(value);
    }

    private static CustomerReturnStatus fromText(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            throw InvalidCustomerReturnDataException.emptyField("Trạng thái trả hàng");
        }
        String normalized = value.trim().toUpperCase();
        for (CustomerReturnStatus status : values()) {
            if (status.name().equals(normalized) || status.code.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw InvalidCustomerReturnDataException.invalidStatus(value);
    }
}
