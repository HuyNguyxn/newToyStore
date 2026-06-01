package com.example.new_toy_store.category.application;

import com.example.new_toy_store.category.application.dto.request.CategoryRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryResponse;
import com.example.new_toy_store.category.domain.Category;

public class CategoryMapper {

    public static Category toEntity(CategoryRequest request) {
        return new Category(
                request.getName(),
                request.getSlug(),
                request.getDescription()
        );
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParent() != null ? category.getParent().getId() : null
        );
    }
}