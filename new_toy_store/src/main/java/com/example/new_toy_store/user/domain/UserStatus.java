package com.example.new_toy_store.user.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserStatus {

    UNVERIFIED("Chưa xác thực email") {
        @Override
        public boolean canLogin() {
            return false;
        }

        @Override
        public boolean canPlaceOrder() {
            return false;
        }

        @Override
        public boolean canModifyData() {
            return true;
        }

        @Override
        public List<UserStatus> getNextValidStates() {
            return List.of(ACTIVE, LOCKED);
        }
    },

    ACTIVE("Đang hoạt động") {
        @Override
        public boolean canLogin() {
            return true;
        }

        @Override
        public boolean canPlaceOrder() {
            return true;
        }

        @Override
        public boolean canModifyData() {
            return true;
        }

        @Override
        public List<UserStatus> getNextValidStates() {
            return List.of(LOCKED);
        }
    },

    LOCKED("Tài khoản bị khóa") {
        @Override
        public boolean canLogin() {
            return false;
        }

        @Override
        public boolean canPlaceOrder() {
            return false;
        }

        @Override
        public boolean canModifyData() {
            return false;
        }

        @Override
        public List<UserStatus> getNextValidStates() {
            return List.of(ACTIVE);
        }
    };

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean canLogin();
    public abstract boolean canPlaceOrder();
    public abstract boolean canModifyData();
    public abstract List<UserStatus> getNextValidStates();

    public boolean canChangeTo(UserStatus targetStatus) {
        return targetStatus != null && getNextValidStates().contains(targetStatus);
    }

    @JsonCreator
    public static UserStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái người dùng không được để trống");
        }
        try {
            return UserStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái người dùng không hợp lệ: " + value);
        }
    }
}
