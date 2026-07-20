package com.example.new_toy_store.category.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CategoryUpdateRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 150, message = "Slug không được vượt quá 150 ký tự")
    private String slug;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @Size(max = 255, message = "Đường dẫn icon không được vượt quá 255 ký tự")
    private String iconUrl;

    @Min(value = 0, message = "Thứ tự hiển thị phải lớn hơn hoặc bằng 0")
    private Integer displayOrder;

    @Positive(message = "ID của danh mục cha phải là một số lớn hơn 0")
    private Integer parentId;

    @NotNull(message = "Version không được để trống để đảm bảo tính toàn vẹn dữ liệu")
    private Integer version;

    public CategoryUpdateRequest() {}

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Integer getParentId() { return parentId; }
    public Integer getVersion() { return version; }

    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setDescription(String description) { this.description = description; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public void setVersion(Integer version) { this.version = version; }
}