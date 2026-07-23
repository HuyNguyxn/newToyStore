package com.example.new_toy_store.category.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CategoryMoveRequest {

    @Positive(message = "ID của danh mục cha phải là một số lớn hơn 0")
    private Integer parentId;

    @NotNull(message = "Thứ tự hiển thị là bắt buộc")
    @Min(value = 0, message = "Thứ tự hiển thị phải lớn hơn hoặc bằng 0")
    private Integer displayOrder;

    @NotNull(message = "Version là bắt buộc để tránh lỗi đồng bộ cấu trúc cây")
    private Long version;

    public CategoryMoveRequest() {}

    public Integer getParentId() { return parentId; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Long getVersion() { return version; }

    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public void setVersion(Long version) { this.version = version; }
}
