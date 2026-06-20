package com.example.new_toy_store.category.application.dto.response;

import java.util.List;

public class CategoryResponse {

    private Integer id;
    private String name;
    private String slug;
    private String description;
    private Integer parentId;
    private List<CategoryResponse> children;

    public CategoryResponse(Integer id, String name, String slug, String description, Integer parentId, List<CategoryResponse> children) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.parentId = parentId;
        this.children = children;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public Integer getParentId() { return parentId; }
    public List<CategoryResponse> getChildren() { return children; }
}