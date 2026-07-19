package com.example.new_toy_store.category.application.service;


import com.example.new_toy_store.category.application.dto.request.CategoryRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryResponse;
import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryRepository;
import com.example.new_toy_store.category.domain.exception.DuplicateCategorySlugException;
import com.example.new_toy_store.category.domain.exception.CategoryNotFoundException;
import com.example.new_toy_store.category.mapper.CategoryMapper;
import com.example.new_toy_store.global.event.CategoryCreatedEvent;
import com.example.new_toy_store.global.event.CategoryStateChangedEvent;
import com.example.new_toy_store.global.event.CategoryUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public CategoryService(CategoryRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (repository.existsBySlug(request.getSlug())) {
            throw new DuplicateCategorySlugException(request.getSlug());
        }

        Category category = CategoryMapper.toEntity(request);

        if (request.getParentId() != null) {
            Category parent = getCategoryEntity(request.getParentId());
            category.assignParent(parent);
        }

        repository.save(category);

        eventPublisher.publishEvent(new CategoryCreatedEvent(
                category.getId(),
                category.getSlug(),
                category.getPath()
        ));

        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse update(Integer id, CategoryRequest request) {
        Category category = getCategoryEntity(id);

        if (request.getVersion() != null && !request.getVersion().equals(category.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Category.class, id);
        }

        if (!category.getSlug().equals(request.getSlug()) && repository.existsBySlug(request.getSlug())) {
            throw new DuplicateCategorySlugException(request.getSlug());
        }

        String oldPath = category.getPath();

        category.update(
                request.getName(),
                request.getSlug(),
                request.getDescription(),
                request.getIconUrl(),
                request.getDisplayOrder()
        );

        if (request.getParentId() != null) {
            Category parent = getCategoryEntity(request.getParentId());
            category.assignParent(parent);
        } else {
            category.removeParent();
        }

        repository.save(category);

        boolean pathChanged = oldPath != null && !oldPath.equals(category.getPath());
        eventPublisher.publishEvent(new CategoryUpdatedEvent(
                category.getId(),
                oldPath,
                category.getPath(),
                pathChanged
        ));

        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public void hideCategory(Integer id) {
        Category category = getCategoryEntity(id);
        category.hide();
        repository.save(category);
        eventPublisher.publishEvent(new CategoryStateChangedEvent(id, "HIDDEN"));
    }

    @Transactional
    public void showCategory(Integer id) {
        Category category = getCategoryEntity(id);
        category.show();
        repository.save(category);

        eventPublisher.publishEvent(new CategoryStateChangedEvent(id, "VISIBLE"));
    }

    @Transactional
    public void delete(Integer id) {
        Category category = getCategoryEntity(id);
        category.delete();
        repository.save(category);

        eventPublisher.publishEvent(new CategoryStateChangedEvent(id, "DELETED"));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Integer id) {
        return CategoryMapper.toResponse(getCategoryEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(Specification<Category> spec, Pageable pageable) {
        return repository.findAll(spec, pageable)
                .map(CategoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        List<Category> rootCategories = repository.findByParentIsNullOrderByDisplayOrderAsc();
        return rootCategories.stream()
                .map(CategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Category getCategoryEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}