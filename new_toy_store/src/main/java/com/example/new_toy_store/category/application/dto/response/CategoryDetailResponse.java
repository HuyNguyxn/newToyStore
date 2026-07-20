package com.example.new_toy_store.category.application.dto.response;

import com.example.new_toy_store.category.domain.CategoryStatus;
import java.util.List;

public class CategoryDetailResponse {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private Integer displayOrder;
    private Integer level;
    private String path;
    private CategoryStatus status;
    private List<CategoryStatus> allowedNextActions;
    private Integer parentId;
    private String parentName;
    private List<CategoryDetailResponse> subCategories;

    public CategoryDetailResponse() {}

    public CategoryDetailResponse(Integer id, String name, String slug, String description,
                                  String iconUrl, Integer displayOrder, Integer level, String path,
                                  CategoryStatus status, List<CategoryStatus> allowedNextActions,
                                  Integer parentId, String parentName, List<CategoryDetailResponse> subCategories) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.iconUrl = iconUrl;
        this.displayOrder = displayOrder;
        this.level = level;
        this.path = path;
        this.status = status;
        this.allowedNextActions = allowedNextActions;
        this.parentId = parentId;
        this.parentName = parentName;
        this.subCategories = subCategories;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Integer getLevel() { return level; }
    public String getPath() { return path; }
    public CategoryStatus getStatus() { return status; }
    public List<CategoryStatus> getAllowedNextActions() { return allowedNextActions; }
    public Integer getParentId() { return parentId; }
    public String getParentName() { return parentName; }
    public List<CategoryDetailResponse> getSubCategories() { return subCategories; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setDescription(String description) { this.description = description; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public void setLevel(Integer level) { this.level = level; }
    public void setPath(String path) { this.path = path; }
    public void setStatus(CategoryStatus status) { this.status = status; }
    public void setAllowedNextActions(List<CategoryStatus> allowedNextActions) { this.allowedNextActions = allowedNextActions; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public void setSubCategories(List<CategoryDetailResponse> subCategories) { this.subCategories = subCategories; }
}