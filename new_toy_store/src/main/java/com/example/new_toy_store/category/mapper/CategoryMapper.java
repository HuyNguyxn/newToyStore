package com.example.new_toy_store.category.mapper;

import com.example.new_toy_store.category.application.dto.request.CategoryCreateRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryDetailResponse;
import com.example.new_toy_store.category.application.dto.response.CategorySummaryResponse;
import com.example.new_toy_store.category.domain.Category;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryMapper {

    public CategorySummaryResponse toSummaryResponse(Category entity) {
        if (entity == null) {
            return null;
        }

        Integer parentId = null;
        String parentName = null;
        if (entity.getParent() != null) {
            parentId = entity.getParent().getId();
            parentName = entity.getParent().getName();
        }

        return new CategorySummaryResponse(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getLevel(),
                entity.getStatus(),
                parentId,
                parentName
        );
    }

    public CategoryDetailResponse toDetailResponse(Category entity) {
        if (entity == null) {
            return null;
        }

        Integer parentId = null;
        String parentName = null;
        if (entity.getParent() != null) {
            parentId = entity.getParent().getId();
            parentName = entity.getParent().getName();
        }

        List<CategoryDetailResponse> mappedChildren = Collections.emptyList();

        if (entity.getSubCategories() != null && !entity.getSubCategories().isEmpty()) {
            mappedChildren = entity.getSubCategories().stream()
                    .map(this::toDetailResponse)
                    .collect(Collectors.toList());
        }

        return new CategoryDetailResponse(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getIconUrl(),
                entity.getDisplayOrder(),
                entity.getLevel(),
                entity.getPath(),
                entity.getStatus(),
                entity.getStatus().getNextValidStates(),
                parentId,
                parentName,
                mappedChildren
        );
    }

    public Category toNewEntity(CategoryCreateRequest request) {
        if (request == null) {
            return null;
        }

        return new Category(
                request.getName(),
                request.getSlug(),
                request.getDescription(),
                request.getIconUrl(),
                request.getDisplayOrder()
        );
    }
}