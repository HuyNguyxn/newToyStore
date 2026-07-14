package com.example.new_toy_store.customer_return.domain;

import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum CustomerReturnStatus {
    REQUESTED("Yêu cầu mới") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Arrays.asList(APPROVED, REJECTED, NEEDS_MORE_INFO, CANCELLED); }
    },
    NEEDS_MORE_INFO("Cần bổ sung thông tin") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Arrays.asList(REQUESTED, REJECTED, CANCELLED); }
    },
    APPROVED("Đã duyệt - Chờ nhận hàng") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Arrays.asList(RECEIVED, CANCELLED); }
    },
    RECEIVED("Kho đã nhận - Đang kiểm định (QC)") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Arrays.asList(INSPECTED_OK, INSPECTED_FAILED); }
    },
    INSPECTED_OK("Kiểm định đạt - Chờ xử lý hoàn tiền/đổi trả") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Arrays.asList(REFUNDED, REPLACED); }
    },
    INSPECTED_FAILED("Kiểm định thất bại - Hàng không đúng mô tả") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Arrays.asList(REJECTED, DISPUTED); }
    },
    REJECTED("Từ chối trả hàng") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Arrays.asList(DISPUTED); }
    },
    DISPUTED("Đang tranh chấp / Khiếu nại") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Arrays.asList(REFUNDED, REPLACED, REJECTED); }
    },
    CANCELLED("Đã hủy") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Collections.emptyList(); }
    },
    REFUNDED("Đã hoàn tiền") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Collections.emptyList(); }
    },
    REPLACED("Đã gửi hàng thay thế") {
        @Override public List<CustomerReturnStatus> getNextValidStates() { return Collections.emptyList(); }
    };

    private final String displayName;

    CustomerReturnStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract List<CustomerReturnStatus> getNextValidStates();

    public boolean canTransitionTo(CustomerReturnStatus nextState) {
        return getNextValidStates().contains(nextState);
    }

    @JsonCreator
    public static CustomerReturnStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidCustomerReturnDataException.emptyField("Trạng thái trả hàng");
        }
        try {
            return CustomerReturnStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidCustomerReturnDataException.invalidStatus(value);
        }
    }
}