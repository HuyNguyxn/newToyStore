package com.example.new_toy_store.category.application.dto.response;

import com.example.new_toy_store.category.domain.CategoryStatus;
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
    private CategoryStatus status;
    private List<CategoryStatus> allowedNextActions;
    private Integer parentId;
    private String parentName;

    private List<CategoryResponse> subCategories;

    public CategoryResponse() {}

    public CategoryResponse(Integer id, String name, String slug, String description,
                            String iconUrl, Integer displayOrder, Integer level, String path,
                            CategoryStatus status, List<CategoryStatus> allowedNextActions,
                            Integer parentId, String parentName, List<CategoryResponse> subCategories) {
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
    public List<CategoryResponse> getSubCategories() { return subCategories; }
}