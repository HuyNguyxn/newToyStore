package com.example.new_toy_store.category.mapper;

import com.example.new_toy_store.category.application.dto.request.CategoryRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryResponse;
import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryStatus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryMapper {

    public static Category toEntity(CategoryRequest request) {
        return new Category(
                request.getName(),
                request.getSlug(),
                request.getDescription(),
                request.getIconUrl(),
                request.getDisplayOrder()
        );
    }

    public static CategoryResponse toResponse(Category category) {
        return toResponse(category, false);
    }

    public static CategoryResponse toResponse(Category category, boolean onlyVisible) {
        if (category == null) {
            return null;
        }

        // Xử lý an toàn thông tin danh mục cha
        Integer parentId = null;
        String parentName = null;
        if (category.getParent() != null) {
            parentId = category.getParent().getId();
            parentName = category.getParent().getName();
        }

        // Tích hợp State Machine
        CategoryStatus status = category.getStatus();
        List<CategoryStatus> nextActions = status != null ? status.getNextValidStates() : null;

        // Xử lý danh mục con
        List<CategoryResponse> subCategories = Collections.emptyList();
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            subCategories = category.getSubCategories().stream()
                    .filter(sub -> !onlyVisible || sub.getStatus() == CategoryStatus.VISIBLE)
                    .sorted(Comparator.comparingInt(Category::getDisplayOrder))
                    .map(sub -> toResponse(sub, onlyVisible))
                    .collect(Collectors.toList());
        }

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIconUrl(),
                category.getDisplayOrder(),
                category.getLevel(),
                category.getPath(),
                status,               // Trả về thẳng Enum để tận dụng @JsonFormat
                nextActions,          // Mảng hành động hợp lệ cho Frontend
                parentId,
                parentName,           // Phục vụ hiển thị UI
                subCategories
        );
    }

    public static CategoryResponse toFlatResponse(Category category) {
        if (category == null) {
            return null;
        }

        Integer parentId = null;
        String parentName = null;
        if (category.getParent() != null) {
            parentId = category.getParent().getId();
            parentName = category.getParent().getName();
        }

        CategoryStatus status = category.getStatus();
        List<CategoryStatus> nextActions = status != null ? status.getNextValidStates() : null;

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIconUrl(),
                category.getDisplayOrder(),
                category.getLevel(),
                category.getPath(),
                status,
                nextActions,
                parentId,
                parentName,
                Collections.emptyList()
        );
    }
}