package com.example.new_toy_store.category.application.service;

import com.example.new_toy_store.category.application.dto.request.CategoryRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryResponse;
import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryRepository;
import com.example.new_toy_store.category.domain.CategoryStatus;
import com.example.new_toy_store.global.event.CategoryHiddenOrDeletedEvent;
import com.example.new_toy_store.category.domain.exception.CategoryNotFoundException;
import com.example.new_toy_store.category.domain.exception.DuplicateCategorySlugException;
import com.example.new_toy_store.category.mapper.CategoryMapper;
import com.example.new_toy_store.infrastructure.specification.CategorySpecification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String keyword, String statusValue, Pageable pageable) {
        CategoryStatus status = null;
        if (statusValue != null && !statusValue.trim().isEmpty()) {
            status = CategoryStatus.from(statusValue);
        }

        Specification<Category> spec = Specification.where(CategorySpecification.hasKeyword(keyword))
                .and(CategorySpecification.hasStatus(status));

        return repository.findAll(spec, pageable)
                .map(CategoryMapper::toFlatResponse);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTreeForCustomer() {
        return repository.findVisibleRootCategories().stream()
                .map(category -> CategoryMapper.toResponse(category, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTreeForAdmin() {
        return repository.findAllRootCategories().stream()
                .map(category -> CategoryMapper.toResponse(category, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Integer id) {
        return CategoryMapper.toResponse(getCategoryEntity(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryPath(Integer categoryId) {
        Category current = getCategoryEntity(categoryId);
        List<Category> path = new ArrayList<>();

        while (current != null) {
            path.add(0, current);
            current = current.getParent();
        }

        return path.stream()
                .map(CategoryMapper::toFlatResponse)
                .collect(Collectors.toList());
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

        category.update(request.getName(), request.getSlug(), request.getDescription(), request.getIconUrl(), request.getDisplayOrder());

        if (request.getParentId() != null) {
            Category parent = getCategoryEntity(request.getParentId());
            category.assignParent(parent);
        } else {
            category.removeParent();
        }

        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public void hideCategory(Integer id) {
        Category category = getCategoryEntity(id);
        category.hide();
        repository.save(category);
        eventPublisher.publishEvent(new CategoryHiddenOrDeletedEvent(id, "HIDDEN"));
    }

    @Transactional
    public void showCategory(Integer id) {
        Category category = getCategoryEntity(id);
        category.show();
        repository.save(category);
    }

    @Transactional
    public void delete(Integer id) {
        Category category = getCategoryEntity(id);
        category.delete();
        repository.save(category);
        eventPublisher.publishEvent(new CategoryHiddenOrDeletedEvent(id, "DELETED"));
    }

    private Category getCategoryEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}