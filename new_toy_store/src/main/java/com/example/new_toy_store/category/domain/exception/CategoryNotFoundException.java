package com.example.new_toy_store.category.domain.exception;

public class CategoryNotFoundException extends RuntimeException {

    private final Integer categoryId;

    public CategoryNotFoundException(Integer categoryId) {
        super("Không tìm thấy danh mục (ID: " + categoryId + ").");
        this.categoryId = categoryId;
    }

    public Integer getCategoryId() { return categoryId; }
}