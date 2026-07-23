package com.example.new_toy_store.category.application.service;

import com.example.new_toy_store.category.application.dto.request.CategoryCreateRequest;
import com.example.new_toy_store.category.application.dto.request.CategoryMoveRequest;
import com.example.new_toy_store.category.application.dto.request.CategoryUpdateInfoRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryDetailResponse;
import com.example.new_toy_store.category.application.dto.response.CategorySummaryResponse;
import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryRepository;
import com.example.new_toy_store.category.domain.CategoryStatus;
import com.example.new_toy_store.category.domain.exception.DuplicateCategorySlugException;
import com.example.new_toy_store.category.domain.exception.CategoryNotFoundException;
import com.example.new_toy_store.category.domain.exception.InvalidCategoryOperationException;
import com.example.new_toy_store.category.mapper.CategoryMapper;
import com.example.new_toy_store.global.event.CategoryCreatedEvent;
import com.example.new_toy_store.global.event.CategoryStateChangedEvent;
import com.example.new_toy_store.global.event.CategoryUpdatedEvent;
import com.example.new_toy_store.infrastructure.specification.CategorySpecification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository repository, ApplicationEventPublisher eventPublisher, CategoryMapper categoryMapper) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryDetailResponse create(CategoryCreateRequest request) {
        if (repository.existsBySlug(request.getSlug())) {
            throw new DuplicateCategorySlugException(request.getSlug());
        }

        Category category = categoryMapper.toNewEntity(request);

        if (request.getParentId() != null) {
            Category parent = getCategoryEntity(request.getParentId());
            category.assignParent(parent);
        }

        repository.save(category);
        eventPublisher.publishEvent(CategoryCreatedEvent.now(category.getId(), category.getSlug(), category.getPath()));

        return categoryMapper.toDetailResponse(category);
    }

    @Transactional
    public CategoryDetailResponse updateInfo(Integer id, CategoryUpdateInfoRequest request) {
        Category category = getCategoryEntity(id);
        checkOptimisticLocking(category.getVersion(), request.getVersion(), id);

        if (!category.getSlug().equals(request.getSlug()) && repository.existsBySlug(request.getSlug())) {
            throw new DuplicateCategorySlugException(request.getSlug());
        }

        String oldPath = category.getPath();
        category.update(
                request.getName(),
                request.getSlug(),
                request.getDescription(),
                request.getIconUrl(),
                category.getDisplayOrder()
        );

        repository.save(category);
        boolean pathChanged = oldPath != null && !oldPath.equals(category.getPath());
        eventPublisher.publishEvent(CategoryUpdatedEvent.now(category.getId(), oldPath, category.getPath(), pathChanged));

        return categoryMapper.toDetailResponse(category);
    }

    @Transactional
    public CategoryDetailResponse moveCategory(Integer id, CategoryMoveRequest request) {
        Category category = getCategoryEntity(id);
        checkOptimisticLocking(category.getVersion(), request.getVersion(), id);

        String oldPath = category.getPath();
        category.update(
                category.getName(), category.getSlug(), category.getDescription(),
                category.getIconUrl(), request.getDisplayOrder()
        );

        if (request.getParentId() != null) {
            Category parent = getCategoryEntity(request.getParentId());
            category.assignParent(parent);
        } else {
            category.removeParent();
        }

        repository.save(category);
        boolean pathChanged = oldPath != null && !oldPath.equals(category.getPath());
        if (pathChanged) {
            eventPublisher.publishEvent(CategoryUpdatedEvent.now(category.getId(), oldPath, category.getPath(), true));
        }

        return categoryMapper.toDetailResponse(category);
    }

    @Transactional
    public void hideCategory(Integer id) {
        Category category = getCategoryEntity(id);
        CategoryStatus previousStatus = category.getStatus();
        if (!updateStatusWithOptimisticLock(category, CategoryStatus.HIDDEN)) {
            return;
        }
        eventPublisher.publishEvent(CategoryStateChangedEvent.now(
                id,
                previousStatus,
                CategoryStatus.HIDDEN,
                category.getPath()
        ));
    }

    @Transactional
    public void showCategory(Integer id) {
        Category category = getCategoryEntity(id);
        if (category.getParent() != null && category.getParent().getStatus() == CategoryStatus.HIDDEN) {
            throw InvalidCategoryOperationException.parentIsHidden(id);
        }
        CategoryStatus previousStatus = category.getStatus();
        if (!updateStatusWithOptimisticLock(category, CategoryStatus.VISIBLE)) {
            return;
        }
        eventPublisher.publishEvent(CategoryStateChangedEvent.now(
                id,
                previousStatus,
                CategoryStatus.VISIBLE,
                category.getPath()
        ));
    }

    @Transactional
    public void delete(Integer id) {
        Category category = getCategoryEntity(id);
        CategoryStatus previousStatus = category.getStatus();
        category.delete();
        repository.save(category);
        eventPublisher.publishEvent(CategoryStateChangedEvent.now(
                id,
                previousStatus,
                CategoryStatus.DELETED,
                category.getPath()
        ));
    }

    @Transactional(readOnly = true)
    public CategoryDetailResponse getById(Integer id) {
        return categoryMapper.toDetailResponse(getCategoryEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<CategorySummaryResponse> searchCategories(String keyword, String status, Pageable pageable) {
        return repository.findAll(CategorySpecification.filter(keyword, status), pageable)
                .map(categoryMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public List<CategoryDetailResponse> getCategoryTreeForAdmin() {
        List<Category> rootCategories = repository.findByParentIsNullOrderByDisplayOrderAsc();
        return rootCategories.stream()
                .map(categoryMapper::toDetailResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDetailResponse> getCategoryTreeForCustomer() {
        List<Category> rootCategories = repository.findByParentIsNullOrderByDisplayOrderAsc();
        return rootCategories.stream()
                .filter(category -> category.getStatus() != CategoryStatus.HIDDEN)
                .map(categoryMapper::toDetailResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getCategoryPath(Integer id) {
        Category current = getCategoryEntity(id);
        List<Category> path = new ArrayList<>();

        while (current != null) {
            path.add(0, current);
            current = current.getParent();
        }

        return path.stream()
                .map(categoryMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    private Category getCategoryEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private void checkOptimisticLocking(Long currentVersion, Long requestVersion, Integer id) {
        if (requestVersion != null && !requestVersion.equals(currentVersion)) {
            throw new ObjectOptimisticLockingFailureException(Category.class, id);
        }
    }

    private boolean updateStatusWithOptimisticLock(Category category, CategoryStatus targetStatus) {
        if (category.getStatus() == targetStatus) {
            return false;
        }

        if (!category.getStatus().canTransitionTo(targetStatus)) {
            throw InvalidCategoryOperationException.invalidStatusTransition(
                    category.getStatus().getName(),
                    targetStatus.getName()
            );
        }

        int updatedRows = repository.updateStatusWithVersion(
                category.getId(),
                targetStatus,
                category.getVersion()
        );

        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Category.class, category.getId());
        }

        return true;
    }
}
