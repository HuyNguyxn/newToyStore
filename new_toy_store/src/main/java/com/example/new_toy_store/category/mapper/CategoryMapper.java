package com.example.new_toy_store.category.mapper;

import com.example.new_toy_store.category.application.dto.request.CategoryRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryResponse;
import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryStatus;
import java.util.Collections;
import java.util.stream.Collectors;

public class CategoryMapper {

    public static Category toEntity(CategoryRequest request) {
        return new Category(
                request.getName(),
                request.getSlug(),
                request.getDescription()
        );
    }

    public static CategoryResponse toResponse(Category category) {
        return toResponse(category, false);
    }

    public static CategoryResponse toResponse(Category category, boolean onlyVisible) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getStatus().getDisplayName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getSubCategories() != null && !category.getSubCategories().isEmpty() ?
                        category.getSubCategories().stream()
                                .filter(sub -> !onlyVisible || sub.getStatus() == CategoryStatus.VISIBLE)
                                .map(sub -> toResponse(sub, onlyVisible))
                                .collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }
}