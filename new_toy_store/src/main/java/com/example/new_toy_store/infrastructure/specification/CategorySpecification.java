package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryStatus;
import com.example.new_toy_store.global.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

public final class CategorySpecification {

    private CategorySpecification() {}

    public static Specification<Category> filter(String keyword, String status) {
        return Specification.where(hasKeyword(keyword))
                .and(hasStatus(status));
    }

    public static Specification<Category> hasKeyword(String keyword) {
        return BaseSpecification.contains("name", keyword);
    }

    public static Specification<Category> hasStatus(CategoryStatus status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<Category> hasStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        return hasStatus(CategoryStatus.from(status));
    }
}
