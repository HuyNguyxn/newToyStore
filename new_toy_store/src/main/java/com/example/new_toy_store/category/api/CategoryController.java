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
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return service.getAllCategories(pageable);
    }

    @GetMapping("/tree")
    public List<CategoryResponse> getCategoryTree() {
        return service.getCategoryTree();
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(@PathVariable Integer id) {
        return service.getCategory(id);
    }

    @PostMapping
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Integer id, @Valid @RequestBody CategoryRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}