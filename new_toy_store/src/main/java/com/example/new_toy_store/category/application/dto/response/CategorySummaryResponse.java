package com.example.new_toy_store.category.application.dto.response;

import com.example.new_toy_store.category.domain.CategoryStatus;

public class CategorySummaryResponse {
    private Integer id;
    private String name;
    private String slug;
    private Integer level;
    private CategoryStatus status;
    private Integer parentId;
    private String parentName;

    public CategorySummaryResponse() {}

    public CategorySummaryResponse(Integer id, String name, String slug, Integer level, CategoryStatus status, Integer parentId, String parentName) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.level = level;
        this.status = status;
        this.parentId = parentId;
        this.parentName = parentName;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public Integer getLevel() { return level; }
    public CategoryStatus getStatus() { return status; }
    public Integer getParentId() { return parentId; }
    public String getParentName() { return parentName; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setLevel(Integer level) { this.level = level; }
    public void setStatus(CategoryStatus status) { this.status = status; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public void setParentName(String parentName) { this.parentName = parentName; }
}