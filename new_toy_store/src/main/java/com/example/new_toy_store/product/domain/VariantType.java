package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum VariantType {

    DEFAULT("default", "Mặc định") {
        @Override
        public boolean canAddAttributes() { return false; }

        @Override
        public List<VariantType> getNextValidStates() {
            // Không được phép đổi sang kiểu khác
            return List.of(DEFAULT);
        }
    },

    MASTER("master", "Bản chính") {
        @Override
        public boolean canAddAttributes() { return true; }

        @Override
        public List<VariantType> getNextValidStates() {
            return List.of(MASTER, REGULAR);
        }
    },

    REGULAR("regular", "Bản thường") {
        @Override
        public boolean canAddAttributes() { return true; }

        @Override
        public List<VariantType> getNextValidStates() {
            return List.of(REGULAR, MASTER);
        }
    };

    private final String code;
    private final String displayName;

    VariantType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }

    public String getDisplayName() { return displayName; }

    public String getName() { return this.name(); }

    public abstract boolean canAddAttributes();

    public abstract List<VariantType> getNextValidStates();

    public boolean canChangeTo(VariantType newType) {
        return getNextValidStates().contains(newType);
    }

    @JsonCreator
    public static VariantType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidProductOperationException.emptyVariantType();
        }

        for (VariantType type : VariantType.values()) {
            if (type.code.equalsIgnoreCase(value.trim()) || type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw InvalidProductOperationException.invalidVariantType(value);
    }
}