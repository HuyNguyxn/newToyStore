package com.example.new_toy_store.category.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CategoryCreateRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 150, message = "Slug không vượt quá 150 ký tự")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug chỉ được chứa chữ cái thường, số và dấu gạch ngang")
    private String slug;

    @Size(max = 500, message = "Mô tả không vượt quá 500 ký tự")
    private String description;

    @Size(max = 255, message = "Đường dẫn icon không vượt quá 255 ký tự")
    private String iconUrl;

    @Min(value = 0, message = "Thứ tự hiển thị phải lớn hơn hoặc bằng 0")
    private Integer displayOrder = 0;

    @Positive(message = "ID của danh mục cha phải là một số lớn hơn 0")
    private Integer parentId;

    public CategoryCreateRequest() {}

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Integer getParentId() { return parentId; }

    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setDescription(String description) { this.description = description; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
}
