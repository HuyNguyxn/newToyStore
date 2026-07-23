package com.example.new_toy_store.category.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CategoryNotFoundException extends CategoryDomainException {

    private final Integer categoryId;

    public CategoryNotFoundException(Integer categoryId) {
        super(
                HttpStatus.NOT_FOUND,
                "CATEGORY_NOT_FOUND",
                "Không tìm thấy danh mục có ID " + categoryId + ".",
                Map.of(
                        "categoryId", categoryId,
                        "entity", "Category"
                )
        );
        this.categoryId = categoryId;
    }

    public Integer getCategoryId() { return categoryId; }
}
