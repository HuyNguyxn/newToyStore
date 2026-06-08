package com.example.new_toy_store.category.application;

import com.example.new_toy_store.category.application.dto.request.CategoryRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryResponse;
import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryRepository;
import com.example.new_toy_store.category.mapper.CategoryMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return repository.findAll(pageable)
                .map(CategoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Integer id) {
        return CategoryMapper.toResponse(getCategoryEntity(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (repository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Slug already exists");
        }

        Category category = CategoryMapper.toEntity(request);

        if (request.getParentId() != null) {
            Category parent = getCategoryEntity(request.getParentId());
            category.assignParent(parent);
        }

        repository.save(category);
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse update(Integer id, CategoryRequest request) {
        Category category = getCategoryEntity(id);

        if (!category.getSlug().equals(request.getSlug()) && repository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Slug already exists");
        }

        category.update(request.getName(), request.getSlug(), request.getDescription());

        if (request.getParentId() != null) {
            Category parent = getCategoryEntity(request.getParentId());
            category.assignParent(parent);
        } else {
            category.removeParent();
        }

        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(Integer id) {
        Category category = getCategoryEntity(id);
        repository.delete(category);
    }

    private Category getCategoryEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
}