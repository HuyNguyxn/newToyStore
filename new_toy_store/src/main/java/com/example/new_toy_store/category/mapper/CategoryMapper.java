package com.example.new_toy_store.category.mapper;

import com.example.new_toy_store.category.application.dto.request.CategoryCreateRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryDetailResponse;
import com.example.new_toy_store.category.application.dto.response.CategorySummaryResponse;
import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    public CategorySummaryResponse toSummaryResponse(Category category) {
        if (category == null) {
            return null;
        }

        ParentInfo parentInfo = toParentInfo(category);

        return new CategorySummaryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getLevel(),
                category.getStatus(),
                parentInfo.id(),
                parentInfo.name()
        );
    }

    public CategoryDetailResponse toDetailResponse(Category category) {
        if (category == null) {
            return null;
        }

        ParentInfo parentInfo = toParentInfo(category);

        return new CategoryDetailResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIconUrl(),
                category.getDisplayOrder(),
                category.getLevel(),
                category.getPath(),
                category.getStatus(),
                toAllowedNextActions(category.getStatus()),
                parentInfo.id(),
                parentInfo.name(),
                toSubCategoryResponses(category)
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

    private ParentInfo toParentInfo(Category category) {
        Category parent = category.getParent();
        if (parent == null) {
            return new ParentInfo(null, null);
        }
        return new ParentInfo(parent.getId(), parent.getName());
    }

    private List<CategoryDetailResponse> toSubCategoryResponses(Category category) {
        if (category.getSubCategories() == null || category.getSubCategories().isEmpty()) {
            return List.of();
        }

        return category.getSubCategories().stream()
                .map(this::toDetailResponse)
                .toList();
    }

    private List<CategoryStatus> toAllowedNextActions(CategoryStatus status) {
        if (status == null) {
            return List.of();
        }
        return status.getNextValidStates();
    }

    private record ParentInfo(Integer id, String name) {}
}
