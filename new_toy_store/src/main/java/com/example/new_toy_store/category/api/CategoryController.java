package com.example.new_toy_store.category.api;

import com.example.new_toy_store.category.application.CategoryService;
import com.example.new_toy_store.category.application.dto.request.CategoryRequest;
import com.example.new_toy_store.category.application.dto.response.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CategoryResponse> searchCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return service.searchCategories(keyword, status, pageable);
    }

    @GetMapping("/tree")
    public List<CategoryResponse> getCategoryTreeForCustomer() {
        return service.getCategoryTreeForCustomer();
    }

    @GetMapping("/admin/tree")
    public List<CategoryResponse> getCategoryTreeForAdmin() {
        return service.getCategoryTreeForAdmin();
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(@PathVariable Integer id) {
        return service.getCategory(id);
    }

    @GetMapping("/{id}/path")
    public List<CategoryResponse> getCategoryPath(@PathVariable Integer id) {
        return service.getCategoryPath(id);
    }

    @PostMapping
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Integer id, @Valid @RequestBody CategoryRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/hide")
    public void hideCategory(@PathVariable Integer id) {
        service.hideCategory(id);
    }

    @PatchMapping("/{id}/show")
    public void showCategory(@PathVariable Integer id) {
        service.showCategory(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}