package com.example.new_toy_store.category.application.dto.response;

public class CategoryResponse {

    private Integer id;
    private String name;
    private String slug;
    private String description;
    private Integer parentId;

    public CategoryResponse(Integer id, String name, String slug, String description, Integer parentId) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.parentId = parentId;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public Integer getParentId() { return parentId; }
}