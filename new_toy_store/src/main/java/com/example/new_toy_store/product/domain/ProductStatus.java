package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ProductStatus {

    ACTIVE("active", "Đang kinh doanh") {
        @Override
        public boolean canBePurchased() { return true; }

        @Override
        public boolean isVisible() { return true; }

        @Override
        public List<ProductStatus> getNextValidStates() {
            return List.of(ACTIVE, INACTIVE, OUT_OF_STOCK);
        }
    },

    INACTIVE("inactive", "Ngừng kinh doanh") {
        @Override
        public boolean canBePurchased() { return false; }

        @Override
        public boolean isVisible() { return false; }

        @Override
        public List<ProductStatus> getNextValidStates() {
            return List.of(INACTIVE, ACTIVE);
        }
    },

    OUT_OF_STOCK("out_of_stock", "Hết hàng") {
        @Override
        public boolean canBePurchased() { return false; }

        @Override
        public boolean isVisible() { return true; }

        @Override
        public List<ProductStatus> getNextValidStates() {
            return List.of(OUT_OF_STOCK, ACTIVE, INACTIVE);
        }
    };

    private final String code;
    private final String displayName;

    ProductStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }

    public String getDisplayName() { return displayName; }

    public String getName() { return this.name(); }

    public abstract boolean canBePurchased();

    public abstract boolean isVisible();

    public abstract List<ProductStatus> getNextValidStates();

    public boolean canTransitionTo(ProductStatus nextStatus) {
        return getNextValidStates().contains(nextStatus);
    }

    @JsonCreator
    public static ProductStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidProductOperationException.emptyStatus();
        }

        for (ProductStatus status : ProductStatus.values()) {
            if (status.code.equalsIgnoreCase(value.trim()) || status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }

        throw InvalidProductOperationException.invalidStatus(value);
    }
}