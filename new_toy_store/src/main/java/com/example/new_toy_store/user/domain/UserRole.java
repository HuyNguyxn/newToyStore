package com.example.new_toy_store.user.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.new_toy_store.user.domain.exception.InvalidUserOperationException;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserRole {

    CUSTOMER("Khách hàng") {
        @Override
        public boolean canManageProducts() {
            return false;
        }

        @Override
        public boolean canManageOrders() {
            return false;
        }

        @Override
        public List<UserRole> getAssignableRoles() {
            return List.of();
        }
    },

    STAFF("Nhân viên") {
        @Override
        public boolean canManageProducts() {
            return true;
        }

        @Override
        public boolean canManageOrders() {
            return true;
        }

        @Override
        public List<UserRole> getAssignableRoles() {
            return List.of(CUSTOMER);
        }
    },

    MANAGER("Quản lý") {
        @Override
        public boolean canManageProducts() {
            return true;
        }

        @Override
        public boolean canManageOrders() {
            return true;
        }

        @Override
        public List<UserRole> getAssignableRoles() {
            return List.of(CUSTOMER, STAFF);
        }
    },

    ADMIN("Quản trị viên") {
        @Override
        public boolean canManageProducts() {
            return true;
        }

        @Override
        public boolean canManageOrders() {
            return true;
        }

        @Override
        public List<UserRole> getAssignableRoles() {
            return List.of(CUSTOMER, STAFF, MANAGER, ADMIN);
        }
    };

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean canManageProducts();
    public abstract boolean canManageOrders();
    public abstract List<UserRole> getAssignableRoles();

    public String toAuthority() {
        return "ROLE_" + name();
    }

    @JsonCreator
    public static UserRole from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidUserOperationException.inputDataInvalid("role", "Vai trò người dùng không được để trống");
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidUserOperationException.invalidRole(value);
        }
    }
}
