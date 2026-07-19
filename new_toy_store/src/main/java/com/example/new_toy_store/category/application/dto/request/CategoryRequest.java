package com.example.new_toy_store.category.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Đường dẫn tĩnh không được để trống")
    @Size(max = 150, message = "Đường dẫn tĩnh không được vượt quá 150 ký tự")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Đường dẫn tĩnh chỉ được chứa chữ cái thường, số và dấu gạch ngang")
    private String slug;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @Size(max = 255, message = "Đường dẫn icon không được vượt quá 255 ký tự")
    private String iconUrl;

    private Integer displayOrder;

    @Positive(message = "ID của danh mục cha phải là một số lớn hơn 0")
    private Integer parentId;

    private Long version;

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Integer getParentId() { return parentId; }
    public Long getVersion() { return version; }
}