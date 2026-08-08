package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> isDistinct() {
        return BaseSpecification.isDistinct();
    }

    public static Specification<Product> hasKeyword(String keyword) {
        return BaseSpecification.contains("name", keyword);
    }

    public static Specification<Product> hasStatus(ProductStatus status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<Product> priceBetween(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            double validMin = (minPrice != null && minPrice >= 0) ? minPrice : 0.0;
            if (maxPrice != null && maxPrice >= validMin) {
                return cb.between(root.get("basePrice"), validMin, maxPrice);
            }
            return cb.greaterThanOrEqualTo(root.get("basePrice"), validMin);
        };
    }

    public static Specification<Product> hasCategoryId(Integer categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            Join<Product, Category> categories = root.join("categories");
            return cb.equal(categories.get("id"), categoryId);
        };
    }

    public static Specification<Product> hasCategoryIds(java.util.Collection<Integer> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) return null;
            Join<Product, Category> categories = root.join("categories");
            return categories.get("id").in(categoryIds);
        };
    }

    public static Specification<Product> isFeatured(Boolean isFeatured) {
        return (root, query, cb) -> isFeatured == null ? null : cb.equal(root.get("isFeatured"), isFeatured);
    }
}