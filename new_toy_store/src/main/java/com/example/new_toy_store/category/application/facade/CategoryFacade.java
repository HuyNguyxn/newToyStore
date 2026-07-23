package com.example.new_toy_store.category.application.facade;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryRepository;
import com.example.new_toy_store.category.domain.CategoryStatus;
import com.example.new_toy_store.category.domain.exception.CategoryCrossModuleException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class CategoryFacade {

    private final CategoryRepository repository;

    public CategoryFacade(CategoryRepository repository) {
        this.repository = repository;
    }

    public boolean existsAndVisible(Integer categoryId) {
        if (categoryId == null) return false;

        return repository.findById(categoryId)
                .map(category -> category.getStatus() == CategoryStatus.VISIBLE)
                .orElse(false);
    }

    public List<Integer> getAllSubCategoryIds(Integer rootCategoryId) {
        if (rootCategoryId == null) return List.of();

        String pathCriteria = "/" + rootCategoryId + "/";

        return repository.findAll().stream()
                .filter(c -> c.getPath() != null && c.getPath().contains(pathCriteria))
                .map(Category::getId)
                .collect(Collectors.toList());
    }

    public String getCategoryPath(Integer categoryId) {
        if (categoryId == null) return null;

        return repository.findById(categoryId)
                .map(Category::getPath)
                .orElse(null);
    }

    public List<Category> getExistingCategories(Set<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return List.of();

        List<Category> categories = repository.findAllById(categoryIds);
        Set<Integer> existingCategoryIds = categories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        Set<Integer> missingCategoryIds = new HashSet<>(categoryIds);
        missingCategoryIds.removeAll(existingCategoryIds);
        if (!missingCategoryIds.isEmpty()) {
            throw CategoryCrossModuleException.missingCategories("Product", missingCategoryIds);
        }

        return categories;
    }
}
