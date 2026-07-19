package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryStatus;
import com.example.new_toy_store.global.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {

    public static Specification<Category> hasKeyword(String keyword) {
        return BaseSpecification.contains("name", keyword);
    }

    public static Specification<Category> hasStatus(CategoryStatus status) {
        return BaseSpecification.isEqual("status", status);
    }
}