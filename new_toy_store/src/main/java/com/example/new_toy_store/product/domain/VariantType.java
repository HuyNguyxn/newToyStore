package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;

public enum VariantType {

    DEFAULT("Mặc định") {
        @Override
        public boolean canAddAttributes() {
            return false;
        }

        @Override
        public boolean canChangeTo(VariantType newType) {
            return false;
        }
    },

    MASTER("Bản chính") {
        @Override
        public boolean canAddAttributes() {
            return true;
        }

        @Override
        public boolean canChangeTo(VariantType newType) {
            return newType == REGULAR;
        }
    },

    REGULAR("Bản thường") {
        @Override
        public boolean canAddAttributes() {
            return true;
        }

        @Override
        public boolean canChangeTo(VariantType newType) {
            return newType == MASTER;
        }
    };

    private final String displayName;

    VariantType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean canAddAttributes();
    public abstract boolean canChangeTo(VariantType newType);

    public static VariantType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidProductOperationException.emptyVariantType();
        }
        try {
            return VariantType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidProductOperationException.invalidVariantType(value);
        }
    }
}