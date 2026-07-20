package com.example.new_toy_store.category.api;

import com.example.new_toy_store.category.application.service.CategoryService;
import com.example.new_toy_store.category.application.dto.request.CategoryCreateRequest;
import com.example.new_toy_store.category.application.dto.request.CategoryMoveRequest;
import com.example.new_toy_store.category.application.dto.request.CategoryUpdateInfoRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryDetailResponse;
import com.example.new_toy_store.category.application.dto.response.CategorySummaryResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CategorySummaryResponse> searchCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return service.searchCategories(keyword, status, pageable);
    }

    @GetMapping("/tree")
    public List<CategoryDetailResponse> getCategoryTreeForCustomer() {
        return service.getCategoryTreeForCustomer();
    }

    @GetMapping("/admin/tree")
    public List<CategoryDetailResponse> getCategoryTreeForAdmin() {
        return service.getCategoryTreeForAdmin();
    }

    @GetMapping("/{id}")
    public CategoryDetailResponse getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/path")
    public List<CategorySummaryResponse> getCategoryPath(@PathVariable Integer id) {
        return service.getCategoryPath(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDetailResponse create(@Valid @RequestBody CategoryCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}/info")
    public CategoryDetailResponse updateInfo(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryUpdateInfoRequest request) {
        return service.updateInfo(id, request);
    }

    @PutMapping("/{id}/move")
    public CategoryDetailResponse moveCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryMoveRequest request) {
        return service.moveCategory(id, request);
    }

    @PatchMapping("/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideCategory(@PathVariable Integer id) {
        service.hideCategory(id);
    }

    @PatchMapping("/{id}/show")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void showCategory(@PathVariable Integer id) {
        service.showCategory(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}