package com.example.new_toy_store.category.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotBlank(message = "Category slug is required")
    private String slug;

    private String description;
    private Integer parentId;

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public Integer getParentId() { return parentId; }
}