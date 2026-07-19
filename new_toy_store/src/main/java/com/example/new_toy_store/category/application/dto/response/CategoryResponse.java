package com.example.new_toy_store.category.application.dto.response;

import java.util.List;

public class CategoryResponse {

    private Integer id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private Integer displayOrder;
    private Integer level;
    private String path;
    private String status;
    private Integer parentId;
    private List<CategoryResponse> children;

    public CategoryResponse(Integer id, String name, String slug, String description, String iconUrl, Integer displayOrder, Integer level, String path, String status, Integer parentId, List<CategoryResponse> children) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.iconUrl = iconUrl;
        this.displayOrder = displayOrder;
        this.level = level;
        this.path = path;
        this.status = status;
        this.parentId = parentId;
        this.children = children;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Integer getLevel() { return level; }
    public String getPath() { return path; }
    public String getStatus() { return status; }
    public Integer getParentId() { return parentId; }
    public List<CategoryResponse> getChildren() { return children; }
}