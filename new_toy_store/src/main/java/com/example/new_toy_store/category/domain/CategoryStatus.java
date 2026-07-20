package com.example.new_toy_store.category.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.new_toy_store.category.domain.exception.InvalidCategoryDataException;

import java.util.Collections;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CategoryStatus {

    VISIBLE("Đang hiển thị cho người dùng") {
        @Override
        public boolean isVisible() { return true; }

        @Override
        public List<CategoryStatus> getNextValidStates() {
            return List.of(HIDDEN, DELETED);
        }
    },

    HIDDEN("Đang bị ẩn, người dùng không thể thấy") {
        @Override
        public boolean isVisible() { return false; }

        @Override
        public List<CategoryStatus> getNextValidStates() {
            return List.of(VISIBLE, DELETED);
        }
    },

    DELETED("Đã bị xóa (xóa mềm), không thể khôi phục") {
        @Override
        public boolean isVisible() { return false; }

        @Override
        public List<CategoryStatus> getNextValidStates() {
            return Collections.emptyList();
        }
    };

    private final String description;

    CategoryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return this.name();
    }

    public abstract boolean isVisible();

    public abstract List<CategoryStatus> getNextValidStates();

    public boolean canTransitionTo(CategoryStatus nextState) {
        return getNextValidStates().contains(nextState);
    }

    @JsonCreator
    public static CategoryStatus from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw InvalidCategoryDataException.emptyStatus();
        }
        try {
            return CategoryStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw InvalidCategoryDataException.invalidStatus(value);
        }
    }
}